package net.alex.game.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alex.game.model.Universe;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.persistence.RoleResource;
import net.alex.game.queue.service.UniverseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static net.alex.game.queue.persistence.RoleName.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UniverseControllerTest extends AbstractControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UniverseService service;

    @Test
    void getColoniesList() throws Exception {
        String id = "1";
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id(id).build();
        Set<RoleIn> roles = Set.of(
                RoleIn.builder().name(ADMIN.name()).build(),
                RoleIn.builder().name(OWNER.name()).roleResource(roleResource).build(),
                RoleIn.builder().name(MEMBER.name()).roleResource(roleResource).build(),
                RoleIn.builder().name(WATCHER.name()).roleResource(roleResource).build()
        );

        checkWithPermissions(roles, token -> {
            doReturn(null).when(service).getColoniesList(anyString(), any());
            mockMvc.perform(get("/v1/api/game/universes/"+id+"/colonies").
                            contentType(MediaType.APPLICATION_JSON).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

    @Test
    void createUniverse() throws Exception {
        Set<RoleIn> roles = Set.of(RoleIn.builder().name(USER.name()).build());

        checkWithPermissions(roles, token -> {
            doNothing().when(service).createUniverse(any());
            mockMvc.perform(post("/v1/api/game/universes").
                            contentType(MediaType.APPLICATION_JSON).
                            content(MAPPER.writeValueAsString(Universe.builder().build())).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

    @Test
    void deleteUniverse() throws Exception {
        String id = "1";
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id(id).build();
        Set<RoleIn> roles = Set.of(
                RoleIn.builder().name(ADMIN.name()).build(),
                RoleIn.builder().name(OWNER.name()).roleResource(roleResource).build()
        );

        checkWithPermissions(roles, token -> {
            doNothing().when(service).deleteUniverse(anyString());
            mockMvc.perform(delete("/v1/api/game/universes/" + id).
                            contentType(MediaType.APPLICATION_JSON).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

    @Test
    void startUniverse() throws Exception {
        String id = "1";
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id(id).build();
        Set<RoleIn> roles = Set.of(
                RoleIn.builder().name(ADMIN.name()).build(),
                RoleIn.builder().name(OWNER.name()).roleResource(roleResource).build()
        );

        checkWithPermissions(roles, token -> {
            doNothing().when(service).startUniverse(any());
            mockMvc.perform(post("/v1/api/game/universes/"+id+"/start").
                            contentType(MediaType.APPLICATION_JSON).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

    @Test
    void stopUniverse() throws Exception {
        String id = "1";
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id(id).build();
        Set<RoleIn> roles = Set.of(
                RoleIn.builder().name(ADMIN.name()).build(),
                RoleIn.builder().name(OWNER.name()).roleResource(roleResource).build()
        );

        checkWithPermissions(roles, token -> {
            doNothing().when(service).stopUniverse(any());
            mockMvc.perform(post("/v1/api/game/universes/"+id+"/stop").
                            contentType(MediaType.APPLICATION_JSON).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

    @Test
    void getUniverse() throws Exception {
        String id = "1";
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id(id).build();
        Set<RoleIn> roles = Set.of(
                RoleIn.builder().name(ADMIN.name()).build(),
                RoleIn.builder().name(OWNER.name()).roleResource(roleResource).build(),
                RoleIn.builder().name(MEMBER.name()).roleResource(roleResource).build(),
                RoleIn.builder().name(WATCHER.name()).roleResource(roleResource).build()
        );

        checkWithPermissions(roles, token -> {
            doReturn(null).when(service).getUniverse(anyString());
            mockMvc.perform(get("/v1/api/game/universes/" + id).
                            contentType(MediaType.APPLICATION_JSON).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

    @Test
    void getUniverseList() throws Exception {
        Set<RoleIn> roles = Set.of(RoleIn.builder().name(USER.name()).build());

        checkWithPermissions(roles, token -> {
            doReturn(null).when(service).getUniversesList(any());
            mockMvc.perform(get("/v1/api/game/universes").
                            contentType(MediaType.APPLICATION_JSON).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }
}