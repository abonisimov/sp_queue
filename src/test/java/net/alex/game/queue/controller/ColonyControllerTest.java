package net.alex.game.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alex.game.model.Colony;
import net.alex.game.model.Universe;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.persistence.RoleResource;
import net.alex.game.queue.service.ColonyService;
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


class ColonyControllerTest extends AbstractControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ColonyService service;

    @Test
    void createColony() throws Exception {
        String id = "1";
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id(id).build();
        Set<RoleIn> roles = Set.of(
                RoleIn.builder().name(ADMIN.name()).build(),
                RoleIn.builder().name(OWNER.name()).roleResource(roleResource).build()
        );

        checkWithPermissions(roles, token -> {
            doNothing().when(service).createColony(any());
            mockMvc.perform(post("/v1/api/game/colonies").
                            contentType(MediaType.APPLICATION_JSON).
                            content(MAPPER.writeValueAsString(Colony.builder().universeId(id).build())).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

    @Test
    void deleteColony() throws Exception {
        String id = "1";
        RoleResource roleResource = RoleResource.builder().name(Colony.class.getSimpleName()).id(id).build();
        Set<RoleIn> roles = Set.of(
                RoleIn.builder().name(ADMIN.name()).build(),
                RoleIn.builder().name(OWNER.name()).roleResource(roleResource).build()
        );

        checkWithPermissions(roles, token -> {
            doNothing().when(service).deleteColony(anyString());
            mockMvc.perform(delete("/v1/api/game/colonies/" + id).
                            contentType(MediaType.APPLICATION_JSON).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

    @Test
    void getColony() throws Exception {
        String id = "1";
        RoleResource roleResource = RoleResource.builder().name(Colony.class.getSimpleName()).id(id).build();
        Set<RoleIn> roles = Set.of(
                RoleIn.builder().name(ADMIN.name()).build(),
                RoleIn.builder().name(OWNER.name()).roleResource(roleResource).build(),
                RoleIn.builder().name(MEMBER.name()).roleResource(roleResource).build(),
                RoleIn.builder().name(WATCHER.name()).roleResource(roleResource).build()
        );

        checkWithPermissions(roles, token -> {
            doReturn(null).when(service).getColony(anyString());
            mockMvc.perform(get("/v1/api/game/colonies/" + id).
                            contentType(MediaType.APPLICATION_JSON).
                            header(AUTH_TOKEN_HEADER_NAME, token)).
                    andDo(print()).
                    andExpect(status().isOk());
        });
    }

}