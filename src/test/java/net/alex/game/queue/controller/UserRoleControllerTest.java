package net.alex.game.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.service.UserRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static net.alex.game.queue.persistence.RoleName.ADMIN;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRoleControllerTest extends AbstractUserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private UserRoleService service;

    @Test
    void assignRoles() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).assignRoles(anyLong(), anyList());
        mockMvc.perform(put("/v1/api/game/users/1/roles/assign").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token).
                        content(MAPPER.writeValueAsString(Collections.singletonList(new RoleIn(ADMIN.name(), null))))).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void assignRoles_empty_body() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).assignRoles(anyLong(), anyList());
        mockMvc.perform(put("/v1/api/game/users/1/roles/assign").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isBadRequest());
    }

    @Test
    void assignRoles_invalid_role() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).assignRoles(anyLong(), anyList());
        mockMvc.perform(put("/v1/api/game/users/1/roles/assign").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token).
                        content(MAPPER.writeValueAsString(Collections.singletonList(new RoleIn("INVALID", null))))).
                andDo(print()).
                andExpect(status().isBadRequest());
    }

    @Test
    void assignRoles_empty_list() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).assignRoles(anyLong(), anyList());
        mockMvc.perform(put("/v1/api/game/users/1/roles/assign").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token).
                        content(MAPPER.writeValueAsString(Collections.emptyList()))).
                andDo(print()).
                andExpect(status().isBadRequest());
    }

    @Test
    void unassignRoles() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).unassignRoles(anyLong(), anyList());
        mockMvc.perform(delete("/v1/api/game/users/1/roles/unassign").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token).
                        content(MAPPER.writeValueAsString(Collections.singletonList(new RoleIn(ADMIN.name(), null))))).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void unassignRoles_empty_body() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).unassignRoles(anyLong(), anyList());
        mockMvc.perform(delete("/v1/api/game/users/1/roles/unassign").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isBadRequest());
    }

    @Test
    void unassignRoles_invalid_role() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).unassignRoles(anyLong(), anyList());
        mockMvc.perform(delete("/v1/api/game/users/1/roles/unassign").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token).
                        content(MAPPER.writeValueAsString(Collections.singletonList(new RoleIn("INVALID", null))))).
                andDo(print()).
                andExpect(status().isBadRequest());
    }

    @Test
    void unassignRoles_empty_list() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).unassignRoles(anyLong(), anyList());
        mockMvc.perform(delete("/v1/api/game/users/1/roles/unassign").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token).
                        content(MAPPER.writeValueAsString(Collections.emptyList()))).
                andDo(print()).
                andExpect(status().isBadRequest());
    }

    @Test
    void assignRolesCandidates() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doReturn(null).when(service).assignRolesCandidates(anyLong(), any());
        mockMvc.perform(get("/v1/api/game/users/1/roles/assign/candidates").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void unassignRolesCandidates() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doReturn(null).when(service).unassignRolesCandidates(anyLong(), any());
        mockMvc.perform(get("/v1/api/game/users/1/roles/unassign/candidates").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void requestRoot() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);
        doNothing().when(service).requestRoot();
        mockMvc.perform(get("/v1/api/game/users/roles/request_root").
                        contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }
}