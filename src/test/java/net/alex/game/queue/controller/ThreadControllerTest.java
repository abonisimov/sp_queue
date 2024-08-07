package net.alex.game.queue.controller;

import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.service.ThreadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static net.alex.game.queue.persistence.RoleName.ADMIN;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ThreadControllerTest extends AbstractUserTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ThreadService service;

    @Test
    void getThreadStatistics() throws Exception {
        doReturn(null).when(service).getThreadStatistics(anyLong());
        mockMvc.perform(get("/v1/api/game/threads/1").contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).
                andExpect(status().isOk());
    }

    @Test
    void getThreadStatisticsList() throws Exception {
        cleanUserRecords();
        String token = createTokenWithRole(ADMIN);

        doReturn(Collections.emptyList()).when(service).getThreadStatisticsList();
        mockMvc.perform(get("/v1/api/game/threads").contentType(MediaType.APPLICATION_JSON).
                        header(AUTH_TOKEN_HEADER_NAME, token)).
                andDo(print()).
                andExpect(status().isOk());
    }
}