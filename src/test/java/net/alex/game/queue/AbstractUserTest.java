package net.alex.game.queue;

import net.alex.game.queue.model.UserOut;
import net.alex.game.queue.model.UserPasswordIn;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import net.alex.game.queue.persistence.repo.RoleRepo;
import net.alex.game.queue.persistence.repo.TokenRepo;
import net.alex.game.queue.persistence.repo.UserRepo;
import net.alex.game.queue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    protected TokenRepo tokenRepo;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    public void cleanUserRecords() {
        tokenRepo.deleteAll();
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

    public String createTokenByRole(String roleName, Optional<Long> resourceId) {
        UserOut userOut = registerUser();
        Optional<UserEntity> optionalUserEntity = userRepo.findById(userOut.getId());
        UserEntity userEntity = optionalUserEntity.orElseThrow();
        RoleEntity roleEntity = userService.getRole(roleName, resourceId);

        if (userEntity.getRoles().stream().noneMatch(r -> r.equals(roleEntity))) {
            userEntity.getRoles().add(roleEntity);
            userRepo.save(userEntity);
        }

        return tokenRepo.findByUser(userEntity).orElseThrow().getToken();
    }
}
