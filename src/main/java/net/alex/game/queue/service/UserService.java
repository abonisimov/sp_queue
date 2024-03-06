package net.alex.game.queue.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.exception.InvalidCredentialsException;
import net.alex.game.queue.exception.ResourceNotFoundException;
import net.alex.game.queue.model.*;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.TokenEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import net.alex.game.queue.persistence.repo.RoleRepo;
import net.alex.game.queue.persistence.repo.TokenRepo;
import net.alex.game.queue.persistence.repo.UserRepo;
import org.apache.commons.lang3.StringUtils;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;

@Slf4j
@Service
public class UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();
    private static final int TOKEN_BYTES_NUMBER = 24;

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final TokenRepo tokenRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo,
                       RoleRepo roleRepo,
                       TokenRepo tokenRepo,
                       PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserOut register(UserPasswordIn userPasswordIn) {
        log.info("Registering user {} {} ({}), {}", userPasswordIn.getFirstName(),
                userPasswordIn.getLastName(), userPasswordIn.getNickName(), userPasswordIn.getEmail());
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(userPasswordIn.getFirstName());
        userEntity.setLastName(userPasswordIn.getLastName());
        userEntity.setNickName(StringUtils.isNotBlank(userPasswordIn.getNickName()) ?
                userPasswordIn.getNickName() : userPasswordIn.getEmail().split("@")[0]);
        userEntity.setEmail(userPasswordIn.getEmail());
        userEntity.setEnabled(true);
        userEntity.setPassword(passwordEncoder.encode(userPasswordIn.getPassword()));
        userEntity.setRoles(Collections.singletonList(getUserRole()));
        userEntity = userRepo.save(userEntity);

        TokenEntity tokenEntity = new TokenEntity(generateToken(), userEntity);
        tokenRepo.save(tokenEntity);
        return UserOut.fromUserEntity(userEntity);
    }

    @Transactional
    public UserOut login(CredentialsIn credentialsIn, HttpServletResponse response) {
        log.debug("Sing in for user {}", credentialsIn.getEmail());
        Optional<UserEntity> optionalUserEntity = userRepo.findByEmail(credentialsIn.getEmail());
        if (optionalUserEntity.isPresent() && passwordEncoder.
                matches(credentialsIn.getPassword(), optionalUserEntity.get().getPassword())) {
            UserEntity userEntity = optionalUserEntity.get();
            if (userEntity.isEnabled()) {
                Optional<TokenEntity> tokenEntityOptional = tokenRepo.findByUser(userEntity);
                String token;
                TokenEntity tokenEntity;
                if (tokenEntityOptional.isEmpty()) {
                    token = generateToken();
                    tokenEntity = new TokenEntity(token, userEntity);
                } else {
                    tokenEntity = tokenEntityOptional.get();
                    if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
                        token = generateToken();
                    } else {
                        token = tokenEntity.getToken();
                    }
                    tokenEntity.updateToken(token);
                }
                tokenRepo.save(tokenEntity);
                response.addHeader(AUTH_TOKEN_HEADER_NAME, token);
                userEntity.setLastLogin(LocalDateTime.now());
                userRepo.save(userEntity);
                return UserOut.fromUserEntity(userEntity);
            } else {
                throw new AccessRestrictedException();
            }
        } else {
            throw new InvalidCredentialsException();
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordIn resetPasswordIn) {
        log.debug("Resetting password for user {}", resetPasswordIn.getEmail());
        Optional<UserEntity> optionalUserEntity = userRepo.findByEmail(resetPasswordIn.getEmail());
        if (optionalUserEntity.isPresent() && passwordEncoder.
                matches(resetPasswordIn.getOldPassword(), optionalUserEntity.get().getPassword())) {
            UserEntity userEntity = optionalUserEntity.get();
            if (userEntity.isEnabled()) {
                updatePasswordAndToken(userEntity, resetPasswordIn.getPassword());
            } else {
                throw new AccessRestrictedException();
            }
        } else {
            throw new InvalidCredentialsException();
        }
    }

    @Transactional
    public void changeUserStatus(long userId, UserStatusIn userStatusIn) {
        log.info("Changing user's {} status to {}", userId, userStatusIn.getUserStatus());
        Optional<UserEntity> optionalUserEntity = userRepo.findById(userId);
        UserEntity userEntity = optionalUserEntity.orElseThrow(ResourceNotFoundException::new);
        userEntity.setEnabled(userStatusIn.getUserStatus().isEnabled());
        userRepo.save(userEntity);
    }

    @Transactional
    public void deleteUser(long userId) {
        log.warn("Removing user {}", userId);
        Optional<UserEntity> optionalUserEntity = userRepo.findById(userId);
        UserEntity userEntity = optionalUserEntity.orElseThrow(ResourceNotFoundException::new);
        Optional<TokenEntity> optionalTokenEntity = tokenRepo.findByUser(userEntity);
        optionalTokenEntity.ifPresent(tokenRepo::delete);
        userRepo.delete(userEntity);
    }

    @Transactional
    public UserOut getUser(long userId) {
        return UserOut.fromUserEntity(userRepo.findById(userId).orElseThrow(ResourceNotFoundException::new));
    }

    @Transactional
    public Page<UserOut> getUsers(Pageable pageable) {
        return userRepo.findAll(pageable).map(UserOut::fromUserEntity);
    }

    @Transactional
    public UserOut changeUser(long userId, UserIn userIn) {
        UserEntity userEntity = userRepo.findById(userId).orElseThrow(ResourceNotFoundException::new);
        userEntity.setFirstName(userIn.getFirstName());
        userEntity.setLastName(userIn.getLastName());
        userEntity.setNickName(StringUtils.isNotBlank(userIn.getNickName()) ?
                userIn.getNickName() : userIn.getEmail().split("@")[0]);
        userEntity.setEmail(userIn.getEmail());
        return UserOut.fromUserEntity(userEntity);
    }

    @Transactional
    public boolean isTokenValid(String token, HttpServletResponse response) {
        Optional<TokenEntity> optionalTokenEntity = tokenRepo.findByToken(token);
        if (optionalTokenEntity.isPresent() &&
                (optionalTokenEntity.get().getExpiryDate().isAfter(LocalDateTime.now())) &&
                (optionalTokenEntity.get().getUser().isEnabled())) {
            response.addHeader(AUTH_TOKEN_HEADER_NAME, token);
            return true;

        }
        return false;
    }

    @Transactional
    public void restorePassword(String email) {
        UserEntity userEntity = userRepo.findByEmail(email).orElseThrow(ResourceNotFoundException::new);
        if (userEntity.isEnabled()) {
            String password = generatePassword();
            updatePasswordAndToken(userEntity, password);
            sendRestorePasswordMail(UserOut.fromUserEntity(userEntity), password);
        } else {
            throw new AccessRestrictedException();
        }
    }

    public RoleEntity getUserRole() {
        return getRole("USER", Optional.empty());
    }

    @Transactional
    public RoleEntity getRole(String roleName, Optional<Long> resourceId) {
        Optional<RoleEntity> optionalRole;
        if (resourceId.isPresent()) {
            optionalRole = roleRepo.findByNameAndResourceId(roleName, resourceId.get());
        } else {
            optionalRole = roleRepo.findByName(roleName);
        }
        return optionalRole.orElseGet(() -> roleRepo.save(new RoleEntity(roleName, resourceId.orElse(null))));
    }

    private void updatePasswordAndToken(UserEntity userEntity, String newPassword) {
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userEntity = userRepo.save(userEntity);
        TokenEntity tokenEntity;
        Optional<TokenEntity> tokenEntityOptional = tokenRepo.findByUser(userEntity);
        if (tokenEntityOptional.isPresent()) {
            tokenEntity = tokenEntityOptional.get();
            tokenEntity.updateToken(generateToken());
        } else {
            tokenEntity = new TokenEntity(generateToken(), userEntity);
        }
        tokenRepo.save(tokenEntity);
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES_NUMBER];
        SECURE_RANDOM.nextBytes(randomBytes);
        return BASE64_ENCODER.encodeToString(randomBytes);
    }

    private String generatePassword() {
        List<CharacterRule> rules = Arrays.asList(
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1));
        PasswordGenerator generator = new PasswordGenerator();
        return generator.generatePassword(12, rules);
    }

    private void sendRestorePasswordMail(UserOut fromUserEntity, String password) {
        // todo: implement
    }
}
