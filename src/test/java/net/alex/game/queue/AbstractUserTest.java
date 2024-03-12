package net.alex.game.queue;

import net.alex.game.queue.model.in.UserPasswordIn;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.entity.*;
import net.alex.game.queue.persistence.repo.*;
import net.alex.game.queue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractUserTest {

    public static final String NAME = "Alex";
    public static final String LAST_NAME = "Test";
    public static final String NICK = "Nick";
    public static final String TOKEN = "token";
    public static final String VALID_EMAIL = "test@test.com";
    public static final String VALID_PASSWORD = "SomeStrongPass1!";

    @Autowired
    protected UserService userService;
    @Autowired
    protected UserRepo userRepo;
    @Autowired
    protected RoleRepo roleRepo;
    @Autowired
    protected AccessTokenRepo accessTokenRepo;
    @Autowired
    protected PasswordTokenRepo passwordTokenRepo;
    @Autowired
    protected RegistrationTokenRepo registrationTokenRepo;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    public void cleanUserRecords() {
        accessTokenRepo.deleteAll();
        passwordTokenRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();
        registrationTokenRepo.deleteAll();
    }

    public UserPasswordIn createUserPasswordIn() {
        return UserPasswordIn.builder()
                .firstName(NAME)
                .lastName(LAST_NAME)
                .nickName(NICK)
                .password(VALID_PASSWORD)
                .matchingPassword(VALID_PASSWORD)
                .build();
    }

    public UserOut registerUser() {
        String token = UUID.randomUUID().toString();
        saveRegistrationToken(VALID_EMAIL, token, LocalDateTime.now().plusMinutes(1));
        return userService.register(token, createUserPasswordIn());
    }

    public UserOut registerUniqueUser(String email, String nickName) {
        UserPasswordIn userPasswordIn = createUserPasswordIn().toBuilder().nickName(nickName).build();
        String token = UUID.randomUUID().toString();
        saveRegistrationToken(email, token, LocalDateTime.now().plusMinutes(1));
        return userService.register(token, userPasswordIn);
    }

    public UserOut registerDisabledUser() {
        String token = UUID.randomUUID().toString();
        saveRegistrationToken(VALID_EMAIL, token, LocalDateTime.now().plusMinutes(1));
        UserOut userOut = userService.register(token, createUserPasswordIn());

        Optional<UserEntity> optionalUserEntity = userRepo.findById(userOut.getId());
        UserEntity userEntity = optionalUserEntity.orElseThrow();
        userEntity.setEnabled(false);
        userEntity = userRepo.save(userEntity);

        return UserOut.fromUserEntity(userEntity);
    }

    public String createTokenByRoleName(String roleName) {
        return createTokenByRole(roleName, Optional.empty());
    }

    public String createTokenByRole(String roleName,
                                    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Long> resourceId) {
        UserOut userOut = registerUser();
        return createToken(userOut, roleName, resourceId);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public String createTokenByRole(String nickName,
                                    String email,
                                    String roleName,
                                    Optional<Long> resourceId) {
        UserOut userOut = registerUniqueUser(nickName, email);
        return createToken(userOut, roleName, resourceId);
    }

    public void saveAccessToken(String token,
                                UserEntity user,
                                LocalDateTime expiryDate) {
        AccessTokenEntity accessTokenEntity = new AccessTokenEntity();
        accessTokenEntity.setToken(token);
        accessTokenEntity.setUser(user);
        accessTokenEntity.setExpiryDate(expiryDate);
        accessTokenRepo.save(accessTokenEntity);
    }

    public void savePasswordToken(String token,
                                  UserEntity user,
                                  LocalDateTime expiryTime) {
        PasswordTokenEntity passwordTokenEntity = new PasswordTokenEntity();
        passwordTokenEntity.setToken(token);
        passwordTokenEntity.setUser(user);
        passwordTokenEntity.setExpiryTime(expiryTime);
        passwordTokenRepo.save(passwordTokenEntity);
    }

    public void saveRegistrationToken(String email,
                                      String token,
                                      LocalDateTime expiryTime) {
        RegistrationTokenEntity registrationTokenEntity = new RegistrationTokenEntity();
        registrationTokenEntity.setToken(token);
        registrationTokenEntity.setEmail(email);
        registrationTokenEntity.setExpiryTime(expiryTime);
        registrationTokenRepo.save(registrationTokenEntity);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private String createToken(UserOut userOut, String roleName, Optional<Long> resourceId) {
        Optional<UserEntity> optionalUserEntity = userRepo.findById(userOut.getId());
        UserEntity userEntity = optionalUserEntity.orElseThrow();
        RoleEntity roleEntity = userService.getRole(roleName, resourceId);

        if (userEntity.getRoles().stream().noneMatch(r -> r.equals(roleEntity))) {
            userEntity.getRoles().add(roleEntity);
            userRepo.save(userEntity);
        }

        return accessTokenRepo.findByUser(userEntity).orElseThrow().getToken();
    }
}
