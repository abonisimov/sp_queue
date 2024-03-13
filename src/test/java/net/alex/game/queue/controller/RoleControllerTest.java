package net.alex.game.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static net.alex.game.queue.persistence.RoleName.ADMIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerTest extends AbstractUserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private RoleService service;

    @Test
    void getRoles() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doReturn(null).when(service).getRoles(any());
        mockMvc.perform(get("/v1/api/game/roles").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void getUsers() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doReturn(null).when(service).getUsers(anyLong(), any());
        mockMvc.perform(get("/v1/api/game/roles/1/users").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }
}