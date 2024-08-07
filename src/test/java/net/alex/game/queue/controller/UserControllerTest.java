package net.alex.game.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.model.UserStatus;
import net.alex.game.queue.model.in.*;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Locale;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static net.alex.game.queue.persistence.RoleName.ADMIN;
import static net.alex.game.queue.persistence.RoleName.USER;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest extends AbstractUserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private UserService service;

    @Test
    void registerRequest() throws Exception {
        doNothing().when(service).registerRequest(anyString(), any());
        mockMvc.perform(get("/v1/api/game/users/register/test@test.com").
                        contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void register() throws Exception {
        UserPasswordIn userPasswordIn = createUserPasswordIn();
        doReturn(UserOut.builder().build()).when(service).register(anyString(), any());
        mockMvc.perform(post("/v1/api/game/users/register/token").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(userPasswordIn))).
                andDo(print()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
    }

    @Test
    void register_validation() throws Exception {
        UserPasswordIn validUser = createUserPasswordIn();
        String uri = "/v1/api/game/users/register/token";
        ResultMatcher expectedStatus = status().isBadRequest();

        testValidation(validUser.toBuilder().firstName(null).build(), post(uri), "firstName", expectedStatus);
        testValidation(validUser.toBuilder().firstName("A").build(), post(uri), "firstName", expectedStatus);
        testValidation(validUser.toBuilder().firstName("A".repeat(51)).build(), post(uri), "firstName", expectedStatus);
        testValidation(validUser.toBuilder().lastName(null).build(), post(uri), "lastName", expectedStatus);
        testValidation(validUser.toBuilder().lastName("A").build(), post(uri), "lastName", expectedStatus);
        testValidation(validUser.toBuilder().lastName("A".repeat(51)).build(), post(uri), "lastName", expectedStatus);
        testValidation(validUser.toBuilder().nickName("AAA").build(), post(uri), "nickName", expectedStatus);
        testValidation(validUser.toBuilder().nickName("A".repeat(51)).build(), post(uri), "nickName", expectedStatus);
        testValidation(validUser.toBuilder().password("weak").matchingPassword("weak").build(), post(uri), "password", expectedStatus);
        testValidation(validUser.toBuilder().matchingPassword(null).build(), post(uri), "matchingPassword", expectedStatus);
        testValidation(validUser.toBuilder().matchingPassword("different").build(), post(uri), "userPasswordIn", expectedStatus);
    }

    @Test
    void login() throws Exception {
        CredentialsIn credentialsIn = CredentialsIn.builder().email("mail@mail.com").password("pass").build();
        doReturn(UserOut.builder().build()).when(service).login(any(), any());
        mockMvc.perform(post("/v1/api/game/users/login").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(credentialsIn))).
                andDo(print()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
    }

    @Test
    void login_validation() throws Exception {
        CredentialsIn validCredentials = CredentialsIn.builder().email("mail@mail.com").password("pass").build();
        String uri = "/v1/api/game/users/login";
        ResultMatcher expectedStatus = status().isBadRequest();

        testValidation(validCredentials.toBuilder().email(null).build(), post(uri), "email", expectedStatus);
        testValidation(validCredentials.toBuilder().password(null).build(), post(uri), "password", expectedStatus);
    }

    @Test
    void resetPassword() throws Exception {
        ResetPasswordIn resetPasswordIn = ResetPasswordIn.
                builder().
                email("mail@mail.com").
                oldPassword("oldPass").
                password(VALID_PASSWORD).
                matchingPassword(VALID_PASSWORD).
                build();
        doNothing().when(service).resetPassword(any());
        mockMvc.perform(post("/v1/api/game/users/resetpassword").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(resetPasswordIn))).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void resetPassword_validation() throws Exception {
        ResetPasswordIn validChangePass = ResetPasswordIn.
                builder().
                email("mail@mail.com").
                oldPassword("oldPass").
                password(VALID_PASSWORD).
                matchingPassword(VALID_PASSWORD).
                build();
        String uri = "/v1/api/game/users/resetpassword";
        ResultMatcher expectedStatus = status().isBadRequest();

        testValidation(validChangePass.toBuilder().email(null).build(), post(uri), "email", expectedStatus);
        testValidation(validChangePass.toBuilder().oldPassword(null).build(), post(uri), "oldPassword", expectedStatus);
        testValidation(validChangePass.toBuilder().password("weak").matchingPassword("weak").build(), post(uri), "password", expectedStatus);
        testValidation(validChangePass.toBuilder().matchingPassword(null).build(), post(uri), "matchingPassword", expectedStatus);
        testValidation(validChangePass.toBuilder().matchingPassword("different").build(), post(uri), "resetPasswordIn", expectedStatus);
    }

    @Test
    void changeUserStatus() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).changeUserStatus(anyLong(), any());
        mockMvc.perform(put("/v1/api/game/users/1/status").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(new UserStatusIn(UserStatus.DISABLE))).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void changeUserStatus_validation() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);

        mockMvc.perform(put("/v1/api/game/users/1/status").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(new UserStatusIn(null))).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$").value(hasKey("userStatus"))).
                andExpect(jsonPath("$.userStatus").value(notNullValue())).
                andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).deleteUser(anyLong());
        mockMvc.perform(delete("/v1/api/game/users/1").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void getUser() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(USER);
        long userId = accessTokenRepo.findByToken(token).orElseThrow().getUser().getId();

        doReturn(UserOut.builder().build()).when(service).getUser(anyLong());
        mockMvc.perform(get("/v1/api/game/users/" + userId).
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void getUser_foreignId() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(USER);
        long userId = accessTokenRepo.findByToken(token).orElseThrow().getUser().getId() + 1;

        mockMvc.perform(get("/v1/api/game/users/" + userId).
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isForbidden());
    }

    @Test
    void getUser_as_admin() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        long userId = accessTokenRepo.findByToken(token).orElseThrow().getUser().getId();

        doReturn(UserOut.builder().build()).when(service).getUser(anyLong());
        mockMvc.perform(get("/v1/api/game/users/" + userId).
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void getUser_as_admin_foreignId() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        long userId = accessTokenRepo.findByToken(token).orElseThrow().getUser().getId() + 1;

        doReturn(UserOut.builder().build()).when(service).getUser(anyLong());
        mockMvc.perform(get("/v1/api/game/users/" + userId).
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void getUsersList() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doReturn(null).when(service).getUsers(any());
        mockMvc.perform(get("/v1/api/game/users").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void changeUser() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(USER);
        long userId = accessTokenRepo.findByToken(token).orElseThrow().getUser().getId();

        UserIn validUser = UserIn.builder()
                .firstName(NAME)
                .lastName(LAST_NAME)
                .nickName(NICK)
                .locale(Locale.getDefault())
                .build();

        doReturn(UserOut.builder().build()).when(service).changeUser(anyLong(), any());
        mockMvc.perform(put("/v1/api/game/users/" + userId).
                        content(MAPPER.writeValueAsString(validUser)).
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void changeUser_foreignId() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(USER);
        long userId = accessTokenRepo.findByToken(token).orElseThrow().getUser().getId() + 1;

        UserIn validUser = UserIn.builder()
                .firstName(NAME)
                .lastName(LAST_NAME)
                .nickName(NICK)
                .locale(Locale.getDefault())
                .build();

        doReturn(UserOut.builder().build()).when(service).changeUser(anyLong(), any());
        mockMvc.perform(put("/v1/api/game/users/" + userId).
                        content(MAPPER.writeValueAsString(validUser)).
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isForbidden());
    }

    @Test
    void changeUser_validation() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(USER);
        long userId = accessTokenRepo.findByToken(token).orElseThrow().getUser().getId();

        UserIn validUser = UserIn.builder()
                .firstName(NAME)
                .lastName(LAST_NAME)
                .nickName(NICK)
                .locale(Locale.getDefault())
                .build();

        String uri = "/v1/api/game/users/" + userId;
        ResultMatcher expectedStatus = status().isBadRequest();

        testValidation(validUser.toBuilder().firstName(null).build(),
                put(uri).header(AUTH_TOKEN_HEADER_NAME, token), "firstName", expectedStatus);
        testValidation(validUser.toBuilder().firstName("A").build(),
                put(uri).header(AUTH_TOKEN_HEADER_NAME, token), "firstName", expectedStatus);
        testValidation(validUser.toBuilder().firstName("A".repeat(51)).build(),
                put(uri).header(AUTH_TOKEN_HEADER_NAME, token), "firstName", expectedStatus);
        testValidation(validUser.toBuilder().lastName(null).build(),
                put(uri).header(AUTH_TOKEN_HEADER_NAME, token), "lastName", expectedStatus);
        testValidation(validUser.toBuilder().lastName("A").build(),
                put(uri).header(AUTH_TOKEN_HEADER_NAME, token), "lastName", expectedStatus);
        testValidation(validUser.toBuilder().lastName("A".repeat(51)).build(),
                put(uri).header(AUTH_TOKEN_HEADER_NAME, token), "lastName", expectedStatus);
        testValidation(validUser.toBuilder().nickName("AAA").build(),
                put(uri).header(AUTH_TOKEN_HEADER_NAME, token), "nickName", expectedStatus);
        testValidation(validUser.toBuilder().nickName("A".repeat(51)).build(),
                put(uri).header(AUTH_TOKEN_HEADER_NAME, token), "nickName", expectedStatus);
    }

    @Test
    void isTokenValid() throws Exception {
        doReturn(true).when(service).isTokenValid(anyString(), any());
        mockMvc.perform(get("/v1/api/game/users/istokenvalid/abc").
                        contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
    }

    @Test
    void restorePasswordRequest() throws Exception {
        doNothing().when(service).restorePasswordRequest(anyString());
        mockMvc.perform(get("/v1/api/game/users/restorepassword/test@test.com").
                        contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void restorePassword() throws Exception {
        PasswordIn validChangePass = PasswordIn.
                builder().
                password(VALID_PASSWORD).
                matchingPassword(VALID_PASSWORD).
                build();

        doNothing().when(service).restorePassword(anyString(), any());
        mockMvc.perform(post("/v1/api/game/users/restorepassword/token").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(validChangePass))).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void restorePassword_validation() throws Exception {
        PasswordIn validChangePass = PasswordIn.
                builder().
                password(VALID_PASSWORD).
                matchingPassword(VALID_PASSWORD).
                build();
        String uri = "/v1/api/game/users/restorepassword/token";
        ResultMatcher expectedStatus = status().isBadRequest();

        testValidation(validChangePass.toBuilder().password("weak").matchingPassword("weak").build(), post(uri), "password", expectedStatus);
        testValidation(validChangePass.toBuilder().matchingPassword(null).build(), post(uri), "matchingPassword", expectedStatus);
        testValidation(validChangePass.toBuilder().matchingPassword("different").build(), post(uri), "passwordIn", expectedStatus);
    }

    private void testValidation(Object invalidObject,
                                MockHttpServletRequestBuilder builder,
                                String expectedMessageName,
                                ResultMatcher expectedStatus) throws Exception {
        mockMvc.perform(builder.
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(invalidObject))).
                andDo(print()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$").value(hasKey(expectedMessageName))).
                andExpect(jsonPath("$." + expectedMessageName).value(notNullValue())).
                andExpect(expectedStatus);
    }
}