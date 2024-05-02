package net.alex.game.queue;

import net.alex.game.queue.config.security.AccessTokenService;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.model.in.UserPasswordIn;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.RoleName;
import net.alex.game.queue.persistence.entity.*;
import net.alex.game.queue.persistence.repo.*;
import net.alex.game.queue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
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
    @Autowired
    protected AccessTokenService accessTokenService;

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
                .locale(Locale.getDefault())
                .build();
    }

    public RoleEntity getRole(RoleName roleName, Optional<Long> resourceId) {
        Optional<RoleEntity> optionalRole;
        if (resourceId.isPresent()) {
            optionalRole = roleRepo.findByNameAndResourceId(roleName.name(), resourceId.get());
        } else {
            optionalRole = roleRepo.findByName(roleName.name());
        }
        return optionalRole.orElseGet(() -> roleRepo.
                save(new RoleEntity(roleName.name(), resourceId.orElse(null), roleName.getRank())));
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

    public String createTokenWithRole(RoleName roleName) {
        return createTokenWithRole(roleName, Optional.empty());
    }

    public String createTokenWithRole(RoleName roleName,
                                      Optional<Long> resourceId) {
        UserOut userOut = registerUser();
        return createTokenWithRoleForUser(userOut, roleName, resourceId);
    }

    public String createTokenWithRole(String nickName,
                                      String email,
                                      RoleName roleName,
                                      Optional<Long> resourceId) {
        UserOut userOut = registerUniqueUser(email, nickName);
        return createTokenWithRoleForUser(userOut, roleName, resourceId);
    }

    private String createTokenWithRoleForUser(UserOut userOut, RoleName roleName, Optional<Long> resourceId) {
        UserEntity userEntity = userRepo.findById(userOut.getId()).orElseThrow();
        createOrRetrieveRoleForUser(userOut, roleName, resourceId);
        return accessTokenRepo.findByUser(userEntity).orElseThrow().getToken();
    }

    public UserOut createOrRetrieveRoleForUser(UserOut userOut, RoleName roleName) {
        return createOrRetrieveRoleForUser(userOut, roleName, Optional.empty());
    }

    public UserOut createOrRetrieveRoleForUser(UserOut userOut, RoleName roleName, Optional<Long> resourceId) {
        UserEntity userEntity = userRepo.findById(userOut.getId()).orElseThrow();
        RoleEntity roleEntity = getRole(roleName, resourceId);

        if (userEntity.getRoles().stream().noneMatch(r -> r.equals(roleEntity))) {
            userEntity.getRoles().add(roleEntity);
            return UserOut.fromUserEntity(userRepo.save(userEntity));
        } else {
            return userOut;
        }
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

    public UserOut createUserWithRole(RoleName roleName) {
        return createTargetAndPrincipalWithRoles(
                Collections.singletonList(new RoleIn(roleName.name(), null)),
                Collections.emptyList());
    }

    public UserOut createTargetAndPrincipalWithRoles(RoleName principalRoleName) {
        return createTargetAndPrincipalWithRoles(
                Collections.emptyList(),
                Collections.singletonList(new RoleIn(principalRoleName.name(), null)));
    }

    public UserOut createTargetAndPrincipalWithRoles(RoleName targetRoleName, RoleName principalRoleName) {
        return createTargetAndPrincipalWithRoles(
                Collections.singletonList(new RoleIn(targetRoleName.name(), null)),
                Collections.singletonList(new RoleIn(principalRoleName.name(), null)));
    }

    public UserOut createTargetAndPrincipalWithRoles(List<RoleIn> targetRoles, List<RoleIn> principalRoles) {
        UserOut target = registerUser();

        for (RoleIn roleIn : targetRoles) {
            target = createOrRetrieveRoleForUser(target, RoleName.valueOf(roleIn.getName()), Optional.ofNullable(roleIn.getResourceId()));
        }

        String token;
        if (principalRoles.isEmpty()) {
            token = accessTokenRepo.findByUser(userRepo.findById(target.getId()).orElseThrow()).orElseThrow().getToken();
        } else {
            token = createTokenWithRole(target.getNickName() + "x", target.getEmail() + "x",
                    RoleName.valueOf(principalRoles.get(0).getName()), Optional.ofNullable(principalRoles.get(0).getResourceId()));
            if (principalRoles.size() > 1) {
                UserOut principal = UserOut.fromUserEntity(accessTokenRepo.findByToken(token).orElseThrow().getUser());
                for (int i = 1; i < principalRoles.size(); i++) {
                    RoleIn roleIn = principalRoles.get(i);
                    createOrRetrieveRoleForUser(principal, RoleName.valueOf(roleIn.getName()),
                            Optional.ofNullable(roleIn.getResourceId()));
                }
            }
        }
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTH_TOKEN_HEADER_NAME, token);
        SecurityContextHolder.getContext().setAuthentication(accessTokenService.getAuthentication(request).orElseThrow());

        return target;
    }
}
