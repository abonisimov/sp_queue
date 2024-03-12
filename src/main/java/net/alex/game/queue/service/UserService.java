package net.alex.game.queue.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.config.security.TokenAuthentication;
import net.alex.game.queue.exception.*;
import net.alex.game.queue.model.in.*;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.entity.*;
import net.alex.game.queue.persistence.repo.*;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static net.alex.game.queue.persistence.entity.PasswordTokenEntity.TOKEN_TTL_HOURS;

@Slf4j
@Service
public class UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();
    private static final int TOKEN_BYTES_NUMBER = 24;

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final AccessTokenRepo accessTokenRepo;
    private final PasswordTokenRepo passwordTokenRepo;
    private final RegistrationTokenRepo registrationTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public UserService(UserRepo userRepo,
                       RoleRepo roleRepo,
                       AccessTokenRepo accessTokenRepo,
                       PasswordTokenRepo passwordTokenRepo,
                       RegistrationTokenRepo registrationTokenRepo,
                       PasswordEncoder passwordEncoder,
                       MailService mailService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.accessTokenRepo = accessTokenRepo;
        this.passwordTokenRepo = passwordTokenRepo;
        this.registrationTokenRepo = registrationTokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public void registerRequest(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new ConstraintViolationException("Invalid email provided", null);
        }
        assertUniqueEmail(email);
        String token = generateToken();
        RegistrationTokenEntity entity = new RegistrationTokenEntity();
        entity.setToken(token);
        entity.setEmail(email);
        entity.setExpiryTime(LocalDateTime.now().plusHours(TOKEN_TTL_HOURS));
        registrationTokenRepo.save(entity);
        mailService.sendRegistrationMail(token);
    }

    @Transactional
    public UserOut register(@NotNull String token, UserPasswordIn userPasswordIn) {
        log.info("Registering user {} {} ({})", userPasswordIn.getFirstName(),
                userPasswordIn.getLastName(), userPasswordIn.getNickName());
        RegistrationTokenEntity registrationTokenEntity = registrationTokenRepo.findByToken(token).
                orElseThrow(ResourceNotFoundException::new);
        if (registrationTokenEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }
        assertUniqueEmail(registrationTokenEntity.getEmail());
        assertUniqueNickName(userPasswordIn.getNickName());
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(userPasswordIn.getFirstName());
        userEntity.setLastName(userPasswordIn.getLastName());
        userEntity.setNickName(userPasswordIn.getNickName());
        userEntity.setEmail(registrationTokenEntity.getEmail());
        userEntity.setEnabled(true);
        userEntity.setPassword(passwordEncoder.encode(userPasswordIn.getPassword()));
        userEntity.setRoles(Collections.singleton(getUserRole()));
        userEntity = userRepo.save(userEntity);

        AccessTokenEntity accessTokenEntity = new AccessTokenEntity(generateToken(), userEntity);
        accessTokenRepo.save(accessTokenEntity);
        registrationTokenRepo.delete(registrationTokenEntity);
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
                Optional<AccessTokenEntity> tokenEntityOptional = accessTokenRepo.findByUser(userEntity);
                String token;
                AccessTokenEntity accessTokenEntity;
                if (tokenEntityOptional.isEmpty()) {
                    token = generateToken();
                    accessTokenEntity = new AccessTokenEntity(token, userEntity);
                } else {
                    accessTokenEntity = tokenEntityOptional.get();
                    if (accessTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
                        token = generateToken();
                    } else {
                        token = accessTokenEntity.getToken();
                    }
                    accessTokenEntity.updateToken(token);
                }
                accessTokenRepo.save(accessTokenEntity);
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
        assertTargetIsValid(userEntity);
        userEntity.setEnabled(userStatusIn.getUserStatus().isEnabled());
        userRepo.save(userEntity);
    }

    @Transactional
    public void deleteUser(long userId) {
        log.warn("Removing user {}", userId);
        Optional<UserEntity> optionalUserEntity = userRepo.findById(userId);
        UserEntity userEntity = optionalUserEntity.orElseThrow(ResourceNotFoundException::new);
        assertTargetIsValid(userEntity);
        Optional<AccessTokenEntity> optionalTokenEntity = accessTokenRepo.findByUser(userEntity);
        optionalTokenEntity.ifPresent(accessTokenRepo::delete);
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
        assertUniqueNickName(userIn.getNickName());
        UserEntity userEntity = userRepo.findById(userId).orElseThrow(ResourceNotFoundException::new);
        userEntity.setFirstName(userIn.getFirstName());
        userEntity.setLastName(userIn.getLastName());
        userEntity.setNickName(userIn.getNickName());
        userEntity = userRepo.save(userEntity);
        return UserOut.fromUserEntity(userEntity);
    }

    @Transactional
    public boolean isTokenValid(String token, HttpServletResponse response) {
        Optional<AccessTokenEntity> optionalTokenEntity = accessTokenRepo.findByToken(token);
        if (optionalTokenEntity.isPresent() &&
                (optionalTokenEntity.get().getExpiryDate().isAfter(LocalDateTime.now())) &&
                (optionalTokenEntity.get().getUser().isEnabled())) {
            response.addHeader(AUTH_TOKEN_HEADER_NAME, token);
            return true;

        }
        return false;
    }

    @Transactional
    public void restorePasswordRequest(String email) {
        UserEntity userEntity = userRepo.findByEmail(email).orElseThrow(ResourceNotFoundException::new);
        if (userEntity.isEnabled()) {
            String token = generateToken();

            PasswordTokenEntity entity = new PasswordTokenEntity();
            entity.setToken(token);
            entity.setUser(userEntity);
            entity.setExpiryTime(LocalDateTime.now().plusHours(TOKEN_TTL_HOURS));
            passwordTokenRepo.save(entity);
            mailService.sendRestorePasswordMail(UserOut.fromUserEntity(userEntity), token);
        } else {
            throw new AccessRestrictedException();
        }
    }

    @Transactional
    public void restorePassword(String token, PasswordIn passwordIn) {
        PasswordTokenEntity entity = passwordTokenRepo.findByToken(token).
                orElseThrow(ResourceNotFoundException::new);
        if (entity.getUser().isEnabled()) {
            if (entity.getExpiryTime().isAfter(LocalDateTime.now())) {
                updatePasswordAndToken(entity.getUser(), passwordIn.getPassword());
                passwordTokenRepo.delete(entity);
            } else {
                throw new TokenExpiredException();
            }
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

    private void assertUniqueEmail(String email) {
        if (userRepo.existsByEmail(email)) {
            throw new ResourceAlreadyRegisteredException("email", "Specified email is already registered");
        }
    }

    private void assertUniqueNickName(String nickName) {
        if (userRepo.existsByNickName(nickName)) {
            throw new ResourceAlreadyRegisteredException("nickName", "Specified nick name is already registered");
        }
    }

    private void updatePasswordAndToken(UserEntity userEntity, String newPassword) {
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userEntity = userRepo.save(userEntity);
        AccessTokenEntity accessTokenEntity;
        Optional<AccessTokenEntity> tokenEntityOptional = accessTokenRepo.findByUser(userEntity);
        if (tokenEntityOptional.isPresent()) {
            accessTokenEntity = tokenEntityOptional.get();
            accessTokenEntity.updateToken(generateToken());
        } else {
            accessTokenEntity = new AccessTokenEntity(generateToken(), userEntity);
        }
        accessTokenRepo.save(accessTokenEntity);
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES_NUMBER];
        SECURE_RANDOM.nextBytes(randomBytes);
        return BASE64_ENCODER.encodeToString(randomBytes);
    }

    private void assertTargetIsValid(UserEntity target) {
        Set<RoleEntity> roles = target.getRoles();
        boolean isTargetAdmin = roles.stream().anyMatch(r -> r.getName().equals("ADMIN"));
        boolean isTargetRoot = roles.stream().anyMatch(r -> r.getName().equals("ROOT"));

        TokenAuthentication tokenAuthentication = (TokenAuthentication)
                SecurityContextHolder.getContext().getAuthentication();
        boolean isUserRoot = tokenAuthentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROOT"));

        if ((!isUserRoot && (isTargetRoot || isTargetAdmin)) || (isUserRoot && isTargetRoot)) {
            throw new AccessRestrictedException();
        }
    }
}
