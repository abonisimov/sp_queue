package net.alex.game.queue.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.exception.InvalidCredentialsException;
import net.alex.game.queue.model.CredentialsIn;
import net.alex.game.queue.model.UserIn;
import net.alex.game.queue.model.UserOut;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.TokenEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import net.alex.game.queue.persistence.repo.RoleRepo;
import net.alex.game.queue.persistence.repo.TokenRepo;
import net.alex.game.queue.persistence.repo.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

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
    public UserOut register(UserIn userIn) {
        log.info("Registering user {} {} ({}), {}", userIn.getFirstName(),
                userIn.getLastName(), userIn.getNickName(), userIn.getEmail());
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(userIn.getFirstName());
        userEntity.setLastName(userIn.getLastName());
        userEntity.setNickName(userIn.getNickName());
        userEntity.setEmail(userIn.getEmail());
        userEntity.setEnabled(true);
        userEntity.setPassword(passwordEncoder.encode(userIn.getPassword()));
        userEntity.setRoles(Collections.singletonList(getUserRole()));
        userEntity = userRepo.save(userEntity);

        TokenEntity tokenEntity = new TokenEntity(generateToken(), userEntity);
        tokenRepo.save(tokenEntity);
        return UserOut.fromUserEntity(userEntity);
    }

    @Transactional
    public UserOut login(CredentialsIn credentialsIn, HttpServletResponse response) {
        Optional<UserEntity> optionalUserEntity = userRepo.findByNickName(credentialsIn.getNickName());
        if (optionalUserEntity.isPresent() && passwordEncoder.
                matches(credentialsIn.getPassword(), optionalUserEntity.get().getPassword())) {
            UserEntity userEntity = optionalUserEntity.get();
            if (userEntity.isEnabled()) {
                Optional<TokenEntity> tokenEntityOptional = tokenRepo.findByUser(userEntity);
                String token;
                if (tokenEntityOptional.isEmpty()) {
                    token = generateToken();
                    TokenEntity tokenEntity = new TokenEntity(token, userEntity);
                    tokenRepo.save(tokenEntity);
                } else {
                    token = tokenEntityOptional.get().getToken();
                }
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
    public RoleEntity getUserRole() {
        Optional<RoleEntity> optional = roleRepo.findByName("USER");
        return optional.orElseGet(() -> roleRepo.save(new RoleEntity("USER")));
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES_NUMBER];
        SECURE_RANDOM.nextBytes(randomBytes);
        return BASE64_ENCODER.encodeToString(randomBytes);
    }
}
