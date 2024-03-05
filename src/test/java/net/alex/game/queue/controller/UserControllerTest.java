package net.alex.game.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alex.game.queue.model.CredentialsIn;
import net.alex.game.queue.model.UserIn;
import net.alex.game.queue.model.UserOut;
import net.alex.game.queue.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService service;

    @Test
    void registerUserAccount() throws Exception {
        UserIn userIn = UserIn.builder()
                .firstName("Alex")
                .lastName("Test")
                .nickName("Nick")
                .password("SomeStrongPass1!")
                .matchingPassword("SomeStrongPass1!")
                .email("test@test.com")
                .build();

        doReturn(UserOut.builder().build()).when(service).register(any());
        mockMvc.perform(post("/v1/api/game/users/register").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(userIn))).
                andDo(print()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
    }

    @Test
    void registerUserAccount_validation() throws Exception {
        UserIn validUser = UserIn.builder()
                .firstName("Alex")
                .lastName("Test")
                .nickName("Nick")
                .password("SomeStrongPass1!")
                .matchingPassword("SomeStrongPass1!")
                .email("test@test.com")
                .build();

        String uri = "/v1/api/game/users/register";
        ResultMatcher expectedStatus = status().isBadRequest();

        testValidation(validUser.toBuilder().firstName(null).build(), uri, "firstName", expectedStatus);
        testValidation(validUser.toBuilder().firstName("A").build(), uri, "firstName", expectedStatus);
        testValidation(validUser.toBuilder().firstName("A".repeat(256)).build(), uri, "firstName", expectedStatus);
        testValidation(validUser.toBuilder().lastName(null).build(), uri, "lastName", expectedStatus);
        testValidation(validUser.toBuilder().lastName("A").build(), uri, "lastName", expectedStatus);
        testValidation(validUser.toBuilder().lastName("A".repeat(256)).build(), uri, "lastName", expectedStatus);
        testValidation(validUser.toBuilder().nickName(null).build(), uri, "nickName", expectedStatus);
        testValidation(validUser.toBuilder().nickName("AAA").build(), uri, "nickName", expectedStatus);
        testValidation(validUser.toBuilder().nickName("A".repeat(256)).build(), uri, "nickName", expectedStatus);
        testValidation(validUser.toBuilder().password("weak").matchingPassword("weak").build(), uri, "password", expectedStatus);
        testValidation(validUser.toBuilder().matchingPassword(null).build(), uri, "matchingPassword", expectedStatus);
        testValidation(validUser.toBuilder().matchingPassword("different").build(), uri, "userIn", expectedStatus);
        testValidation(validUser.toBuilder().email(null).build(), uri, "email", expectedStatus);
        testValidation(validUser.toBuilder().email("incorrect").build(), uri, "email", expectedStatus);
    }

    @Test
    void login() throws Exception {
        CredentialsIn credentialsIn = CredentialsIn.builder().nickName("nick").password("pass").build();
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
        CredentialsIn validCredentials = CredentialsIn.builder().nickName("nick").password("pass").build();
        String uri = "/v1/api/game/users/login";
        ResultMatcher expectedStatus = status().isBadRequest();

        testValidation(validCredentials.toBuilder().nickName(null).build(), uri, "nickName", expectedStatus);
        testValidation(validCredentials.toBuilder().password(null).build(), uri, "password", expectedStatus);
    }

    private void testValidation(Object invalidObject,
                                String uri,
                                String expectedMessageName,
                                ResultMatcher expectedStatus) throws Exception {
        doReturn(UserOut.builder().build()).when(service).register(any());
        mockMvc.perform(post(uri).
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(invalidObject))).
                andDo(print()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$").value(hasKey(expectedMessageName))).
                andExpect(jsonPath("$." + expectedMessageName).value(notNullValue())).
                andExpect(expectedStatus);
    }
}