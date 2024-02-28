package net.alex.game.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alex.game.model.Colony;
import net.alex.game.queue.QueueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ColonyControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private QueueService service;

    @Test
    void createColony() throws Exception {
        doNothing().when(service).createColony(any());
        mockMvc.perform(post("/v1/api/game/colonies").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(Colony.builder().build()))).
                andDo(print()).andExpect(status().isOk());
    }

    @Test
    void deleteColony() throws Exception {
        doNothing().when(service).deleteColony(anyString());
        mockMvc.perform(delete("/v1/api/game/colonies/1").contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).andExpect(status().isOk());
    }

    @Test
    void getColony() throws Exception {
        doReturn(null).when(service).getColony(anyString());
        mockMvc.perform(get("/v1/api/game/colonies/1").contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).andExpect(status().isOk());
    }
}