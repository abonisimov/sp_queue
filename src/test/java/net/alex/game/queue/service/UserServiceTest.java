package net.alex.game.queue.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.exception.*;
import net.alex.game.queue.model.UserStatus;
import net.alex.game.queue.model.in.*;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.RoleResource;
import net.alex.game.queue.persistence.entity.AccessTokenEntity;
import net.alex.game.queue.persistence.entity.PasswordTokenEntity;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static net.alex.game.queue.persistence.RoleName.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest extends AbstractUserTest {

    @BeforeEach
    void beforeEach() {
        cleanUserRecords();
    }

    @Test
    void registerRequest() {
        userService.registerRequest(VALID_EMAIL, Locale.getDefault());
        assertTrue(registrationTokenRepo.findByEmail(VALID_EMAIL).isPresent());
    }

    @Test
    void registerRequest_invalid_email() {
        Locale locale = Locale.getDefault();
        assertThrows(ConstraintViolationException.class, () -> userService.registerRequest("broken", locale));
    }

    @Test
    void registerRequest_null_email() {
        Locale locale = Locale.getDefault();
        assertThrows(ConstraintViolationException.class, () -> userService.registerRequest(null, locale));
    }

    @Test
    void register() {
        saveRegistrationToken(VALID_EMAIL, TOKEN, LocalDateTime.now().plusMinutes(1));
        UserOut result = userService.register(TOKEN, createUserPasswordIn());

        assertNotNull(result);
        assertEquals(NICK, result.getNickName());
        assertEquals(NAME, result.getFirstName());
        assertEquals(LAST_NAME, result.getLastName());
        assertEquals(VALID_EMAIL, result.getEmail());
        assertEquals(Locale.getDefault(), result.getLocale());
        assertTrue(result.isEnabled());
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertEquals(USER.name(), result.getRoles().get(0).getName());
        assertEquals(USER.getRank(), result.getRoles().get(0).getRank());
        assertTrue(getTokenEntityByUserId(result.getId()).isPresent());

        assertFalse(registrationTokenRepo.findByToken(TOKEN).isPresent());
    }

    @Test
    void register_token_expired() {
        saveRegistrationToken(VALID_EMAIL, TOKEN, LocalDateTime.now().minusMinutes(1));

        UserPasswordIn userPasswordIn = createUserPasswordIn();
        assertThrows(TokenExpiredException.class, () -> userService.register(TOKEN, userPasswordIn));
    }

    @Test
    void register_token_not_found() {
        UserPasswordIn userPasswordIn = createUserPasswordIn();
        assertThrows(ResourceNotFoundException.class, () -> userService.register(TOKEN, userPasswordIn));
    }

    @Test
    void register_invalid_nickName() {
        UserOut userOut = registerUser();
        saveRegistrationToken(VALID_EMAIL, "new_token", LocalDateTime.now().plusMinutes(1));
        UserPasswordIn userPasswordIn = createUserPasswordIn().toBuilder().
                nickName(userOut.getNickName()).build();
        assertThrows(ResourceAlreadyRegisteredException.class, () -> userService.register("new_token", userPasswordIn));
    }

    @Test
    void login() {
        UserOut userOut = registerUser();

        Optional<AccessTokenEntity> tokenEntity = getTokenEntityByUserId(userOut.getId());
        LocalDateTime expiryDate = tokenEntity.orElseThrow().getExpiryDate();

        CredentialsIn credentials = CredentialsIn.builder().email(userOut.getEmail()).
                password(VALID_PASSWORD).build();

        HttpServletResponse response = new MockHttpServletResponse();
        UserOut result = userService.login(credentials, response);
        assertNotNull(result);
        assertEquals(userOut.getNickName(), result.getNickName());
        assertEquals(userOut.getFirstName(), result.getFirstName());
        assertEquals(userOut.getLastName(), result.getLastName());
        assertEquals(userOut.getEmail(), result.getEmail());
        assertEquals(userOut.isEnabled(), result.isEnabled());
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertEquals(USER.name(), result.getRoles().get(0).getName());
        assertNotNull(result.getLastLogin());
        assertTrue(response.containsHeader(AUTH_TOKEN_HEADER_NAME));

        tokenEntity = getTokenEntityByUserId(userOut.getId());
        assertTrue(tokenEntity.isPresent());
        assertTrue(StringUtils.isNotBlank(tokenEntity.get().getToken()));
        assertEquals(tokenEntity.get().getToken(), response.getHeader(AUTH_TOKEN_HEADER_NAME));
        assertNotEquals(expiryDate, tokenEntity.get().getExpiryDate());
    }

    @Test
    void login_invalidUser() {
        CredentialsIn credentials = CredentialsIn.builder().email("mail@mail.com").password("pass").build();
        HttpServletResponse response = new MockHttpServletResponse();
        assertThrows(InvalidCredentialsException.class, () -> userService.login(credentials, response));
    }

    @Test
    void login_invalidPassword() {
        UserOut userOut = registerUser();

        CredentialsIn credentials = CredentialsIn.builder().email(userOut.getEmail()).
                password(VALID_PASSWORD + "1").build();
        HttpServletResponse response = new MockHttpServletResponse();
        assertThrows(InvalidCredentialsException.class, () -> userService.login(credentials, response));
    }

    @Test
    void login_disabledUser() {
        UserOut userOut = registerDisabledUser();

        CredentialsIn credentials = CredentialsIn.builder().email(userOut.getEmail()).password(VALID_PASSWORD).build();
        HttpServletResponse response = new MockHttpServletResponse();
        assertThrows(AccessRestrictedException.class, () -> userService.login(credentials, response));
    }

    @Test
    void resetPassword() {
        UserOut userOut = registerUser();

        ResetPasswordIn resetPasswordIn = ResetPasswordIn.builder().
                email(userOut.getEmail()).
                oldPassword(VALID_PASSWORD).
                password(VALID_PASSWORD + "SomeChange").
                matchingPassword(VALID_PASSWORD + "SomeChange").
                build();

        Optional<UserEntity> optionalUserEntity = userRepo.findById(userOut.getId());
        assertTrue(optionalUserEntity.isPresent());
        Optional<AccessTokenEntity> optionalTokenEntity = accessTokenRepo.findByUser(optionalUserEntity.get());
        assertTrue(optionalTokenEntity.isPresent());

        String password = optionalUserEntity.get().getPassword();
        String token = optionalTokenEntity.get().getToken();

        userService.resetPassword(resetPasswordIn);

        optionalUserEntity = userRepo.findById(userOut.getId());
        assertTrue(optionalUserEntity.isPresent());
        optionalTokenEntity = accessTokenRepo.findByUser(optionalUserEntity.get());
        assertTrue(optionalTokenEntity.isPresent());

        assertNotEquals(password, optionalUserEntity.get().getPassword());
        assertNotEquals(token, optionalTokenEntity.get().getToken());
    }

    @Test
    void resetPassword_invalidUser() {
        ResetPasswordIn resetPasswordIn = ResetPasswordIn.builder().
                email("mail@mail.com").
                oldPassword("oldPass").
                password("pass").
                matchingPassword("pass").
                build();
        assertThrows(InvalidCredentialsException.class, () -> userService.resetPassword(resetPasswordIn));
    }

    @Test
    void resetPassword_invalidPassword() {
        UserOut userOut = registerUser();

        ResetPasswordIn resetPasswordIn = ResetPasswordIn.builder().
                email(userOut.getEmail()).
                oldPassword(VALID_PASSWORD + "1").
                password("pass").
                matchingPassword("pass").
                build();
        assertThrows(InvalidCredentialsException.class, () -> userService.resetPassword(resetPasswordIn));
    }

    @Test
    void resetPassword_disabledUser() {
        UserOut userOut = registerDisabledUser();

        ResetPasswordIn resetPasswordIn = ResetPasswordIn.builder().
                email(userOut.getEmail()).
                oldPassword(VALID_PASSWORD).
                password("pass").
                matchingPassword("pass").
                build();

        assertThrows(AccessRestrictedException.class, () -> userService.resetPassword(resetPasswordIn));
    }

    @Test
    void changeUserStatus() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN);
        long userId = userOut.getId();

        userService.changeUserStatus(userId, new UserStatusIn(UserStatus.DISABLE));
        assertFalse(userRepo.findById(userId).orElseThrow().isEnabled());
        userService.changeUserStatus(userId, new UserStatusIn(UserStatus.ENABLE));
        assertTrue(userRepo.findById(userId).orElseThrow().isEnabled());
    }

    @Test
    void changeUserStatus_invalidUser() {
        UserStatusIn userStatusIn = new UserStatusIn(UserStatus.DISABLE);
        assertThrows(ResourceNotFoundException.class, () -> userService.changeUserStatus(1, userStatusIn));
    }

    @Test
    void changeUserStatus_same_rank() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN, ADMIN);
        long userId = userOut.getId();

        UserStatusIn userStatusIn = new UserStatusIn(UserStatus.DISABLE);
        assertThrows(AccessRestrictedException.class, () -> userService.changeUserStatus(userId, userStatusIn));
    }

    @Test
    void changeUserStatus_more_important_rank() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ROOT, ADMIN);
        long userId = userOut.getId();

        UserStatusIn userStatusIn = new UserStatusIn(UserStatus.DISABLE);
        assertThrows(AccessRestrictedException.class, () -> userService.changeUserStatus(userId, userStatusIn));
    }

    @Test
    void changeUserStatus_self() {
        UserOut userOut = createUserWithRole(ROOT);
        long userId = userOut.getId();

        UserStatusIn userStatusIn = new UserStatusIn(UserStatus.DISABLE);
        assertThrows(AccessRestrictedException.class, () -> userService.changeUserStatus(userId, userStatusIn));
    }

    @Test
    void deleteUser() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN);
        long userId = userOut.getId();

        userService.deleteUser(userId);

        assertTrue(userRepo.findById(userId).isEmpty());
        assertEquals(1, accessTokenRepo.count());
        assertTrue(roleRepo.findAll().iterator().hasNext());
    }

    @Test
    void deleteUser_invalidUser() {
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1));
    }

    @Test
    void deleteUser_same_rank() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN, ADMIN);
        long userId = userOut.getId();
        assertThrows(AccessRestrictedException.class, () -> userService.deleteUser(userId));
    }

    @Test
    void deleteUser_more_important_rank() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ROOT, ADMIN);
        long userId = userOut.getId();

        assertThrows(AccessRestrictedException.class, () -> userService.deleteUser(userId));
    }

    @Test
    void deleteUser_self() {
        UserOut userOut = createUserWithRole(ROOT);
        long userId = userOut.getId();
        assertThrows(AccessRestrictedException.class, () -> userService.deleteUser(userId));
    }

    @Test
    void getUser() {
        UserOut userOut = registerUser();
        long userId = userOut.getId();
        UserOut result = userService.getUser(userId);
        assertEquals(userOut, result);
    }

    @Test
    void getUser_invalidUser() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(1));
    }

    @Test
    void getUsersList() {
        UserOut userOut = registerUser();
        Page<UserOut> result = userService.getUsers(PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userOut, result.iterator().next());
    }

    @Test
    void changeUser() {
        UserOut userOut = registerUser();

        UserIn userIn = UserIn.
                builder().
                firstName(userOut.getFirstName() + 'a').
                lastName(userOut.getLastName() + 'b').
                nickName(userOut.getNickName() + 'c').
                locale(Locale.FRANCE).
                build();

        UserOut result = userService.changeUser(userOut.getId(), userIn);

        assertNotNull(result);
        assertEquals(userIn.getFirstName(), result.getFirstName());
        assertEquals(userIn.getLastName(), result.getLastName());
        assertEquals(userIn.getNickName(), result.getNickName());
        assertEquals(userIn.getLocale(), result.getLocale());

        UserEntity userEntity = userRepo.findById(result.getId()).orElseThrow();
        assertEquals(userIn.getFirstName(), userEntity.getFirstName());
        assertEquals(userIn.getLastName(), userEntity.getLastName());
        assertEquals(userIn.getNickName(), userEntity.getNickName());
        assertEquals(userIn.getLocale(), userEntity.getLocale());

        assertEquals(userOut.getEmail(), userEntity.getEmail());
    }

    @Test
    void changeUser_invalid_nickName() {
        UserOut userOut = registerUser();
        long userId = userOut.getId();
        UserIn userIn = UserIn.builder()
                .firstName(NAME)
                .lastName(LAST_NAME)
                .nickName(userOut.getNickName())
                .locale(Locale.getDefault())
                .build();

        assertThrows(ResourceAlreadyRegisteredException.class, () -> userService.changeUser(userId, userIn));
    }

    @Test
    void isTokenValid() {
        String token = createTokenWithRole(USER);
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertTrue(userService.isTokenValid(token, response));
        assertEquals(token, response.getHeader(AUTH_TOKEN_HEADER_NAME));
    }

    @Test
    void isTokenValid_expired() {
        String token = createTokenWithRole(USER);
        AccessTokenEntity accessTokenEntity = accessTokenRepo.findByToken(token).orElseThrow();
        accessTokenEntity.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        accessTokenRepo.save(accessTokenEntity);
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(userService.isTokenValid(token, response));
        assertFalse(response.containsHeader(AUTH_TOKEN_HEADER_NAME));
    }

    @Test
    void isTokenValid_userBlocked() {
        String token = createTokenWithRole(USER);
        AccessTokenEntity accessTokenEntity = accessTokenRepo.findByToken(token).orElseThrow();
        UserEntity userEntity = accessTokenEntity.getUser();
        userEntity.setEnabled(false);
        userRepo.save(userEntity);
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(userService.isTokenValid(token, response));
        assertFalse(response.containsHeader(AUTH_TOKEN_HEADER_NAME));
    }

    @Test
    void restorePasswordRequest_with_reply() {
        UserOut userOut = registerUser();

        UserEntity userEntity = userRepo.findById(userOut.getId()).orElseThrow();
        AccessTokenEntity tokenEntity = accessTokenRepo.findByUser(userEntity).orElseThrow();

        String password = userEntity.getPassword();
        String token = tokenEntity.getToken();

        userService.restorePasswordRequest(userOut.getEmail());

        userEntity = userRepo.findById(userOut.getId()).orElseThrow();
        PasswordTokenEntity passwordTokenEntity = passwordTokenRepo.
                findByUser(userEntity).orElseThrow();

        assertNotNull(passwordTokenEntity.getToken());
        assertNotNull(passwordTokenEntity.getExpiryTime());

        userService.restorePassword(
                passwordTokenEntity.getToken(),
                PasswordIn.
                        builder().
                        password(VALID_PASSWORD + "1").
                        matchingPassword(VALID_PASSWORD + "1").
                        build());

        userEntity = userRepo.findById(userOut.getId()).orElseThrow();
        tokenEntity = accessTokenRepo.findByUser(userEntity).orElseThrow();

        assertNotEquals(password, userEntity.getPassword());
        assertNotEquals(token, tokenEntity.getToken());

        assertFalse(passwordTokenRepo.findByToken(token).isPresent());
    }

    @Test
    void restorePasswordRequest_disabled_user() {
        UserOut userOut = registerDisabledUser();
        String email = userOut.getEmail();
        assertThrows(AccessRestrictedException.class, () -> userService.restorePasswordRequest(email));
    }

    @Test
    void restorePassword_token_expired() {
        UserOut userOut = registerUser();
        UserEntity userEntity = userRepo.findById(userOut.getId()).orElseThrow();

        savePasswordToken(TOKEN, userEntity, LocalDateTime.now().minusMinutes(1));

        PasswordIn passwordIn = PasswordIn.builder().password(VALID_PASSWORD).matchingPassword(VALID_PASSWORD).build();
        assertThrows(TokenExpiredException.class, () -> userService.restorePassword(TOKEN, passwordIn));
    }

    @Test
    void restorePassword_disabled_user() {
        UserOut userOut = registerDisabledUser();
        UserEntity userEntity = userRepo.findById(userOut.getId()).orElseThrow();

        savePasswordToken(TOKEN, userEntity, LocalDateTime.now().plusHours(1));

        PasswordIn passwordIn = PasswordIn.builder().password(VALID_PASSWORD).matchingPassword(VALID_PASSWORD).build();
        assertThrows(AccessRestrictedException.class, () -> userService.restorePassword(TOKEN, passwordIn));
    }

    @Test
    void restorePassword_token_not_found() {
        PasswordIn passwordIn = PasswordIn.builder().password(VALID_PASSWORD).matchingPassword(VALID_PASSWORD).build();
        assertThrows(ResourceNotFoundException.class, () -> userService.restorePassword(TOKEN, passwordIn));
    }

    @Test
    void getUserRole() {
        RoleEntity roleEntity = getRole(USER, Optional.empty());
        assertNotNull(roleEntity);
        assertNotNull(roleEntity.getId());
        assertEquals(USER.name(), roleEntity.getName());
        assertNull(roleEntity.getResourceId());

        RoleEntity roleEntity2 = getRole(USER, Optional.empty());
        assertNotNull(roleEntity2);
        assertEquals(roleEntity, roleEntity2);
    }

    @Test
    void getRole() {
        RoleEntity roleEntity = getRole(OWNER, Optional.of(RoleResource.builder().name("u").id("10").build()));
        assertNotNull(roleEntity);
        assertNotNull(roleEntity.getId());
        assertEquals(OWNER.name(), roleEntity.getName());
        assertEquals("10", roleEntity.getResourceId());
        assertEquals("u", roleEntity.getResourceName());
        assertEquals(OWNER.getRank(), roleEntity.getRank());

        RoleEntity roleEntity2 = getRole(OWNER, Optional.of(RoleResource.builder().name("u").id("10").build()));
        assertNotNull(roleEntity2);
        assertEquals(roleEntity, roleEntity2);
    }

    private Optional<AccessTokenEntity> getTokenEntityByUserId(long userId) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        return accessTokenRepo.findByUser(userEntity);
    }
}