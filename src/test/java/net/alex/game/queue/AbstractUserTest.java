package net.alex.game.queue;

import net.alex.game.queue.model.in.UserPasswordIn;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.entity.AccessTokenEntity;
import net.alex.game.queue.persistence.entity.RestorePasswordTokenEntity;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import net.alex.game.queue.persistence.repo.AccessTokenRepo;
import net.alex.game.queue.persistence.repo.RestorePasswordTokenRepo;
import net.alex.game.queue.persistence.repo.RoleRepo;
import net.alex.game.queue.persistence.repo.UserRepo;
import net.alex.game.queue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

public abstract class AbstractUserTest {

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
    protected RestorePasswordTokenRepo restorePasswordTokenRepo;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    public void cleanUserRecords() {
        accessTokenRepo.deleteAll();
        restorePasswordTokenRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();
    }

    public UserOut registerUser() {
        return userService.register(UserPasswordIn.builder()
                .firstName("Alex")
                .lastName("Test")
                .nickName("Nick")
                .password(VALID_PASSWORD)
                .matchingPassword(VALID_PASSWORD)
                .email("test@test.com")
                .build());
    }

    public UserOut registerUniqueUser(String email, String nickName) {
        return userService.register(UserPasswordIn.builder()
                .firstName("Alex")
                .lastName("Test")
                .nickName(nickName)
                .password(VALID_PASSWORD)
                .matchingPassword(VALID_PASSWORD)
                .email(email)
                .build());
    }

    public UserOut registerDisabledUser() {
        UserOut userOut = userService.register(UserPasswordIn.builder()
                .firstName("Alex")
                .lastName("Test")
                .nickName("Nick")
                .password(VALID_PASSWORD)
                .matchingPassword(VALID_PASSWORD)
                .email("test@test.com")
                .build());

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

    public void saveRestorePasswordToken(String token,
                                         UserEntity user,
                                         LocalDateTime expiryTime) {
        RestorePasswordTokenEntity restorePasswordTokenEntity = new RestorePasswordTokenEntity();
        restorePasswordTokenEntity.setToken(token);
        restorePasswordTokenEntity.setUser(user);
        restorePasswordTokenEntity.setExpiryTime(expiryTime);
        restorePasswordTokenRepo.save(restorePasswordTokenEntity);
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
