package net.alex.game.queue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alex.game.model.Universe;
import net.alex.game.queue.service.UniverseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UniverseControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UniverseService service;

    @Test
    void getColoniesList() throws Exception {
        doReturn(Collections.emptyList()).when(service).getColoniesList(anyString());
        mockMvc.perform(get("/v1/api/game/universes/1/colonies").contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).andExpect(status().isOk());
    }

    @Test
    void createUniverse() throws Exception {
        doNothing().when(service).createUniverse(any());
        mockMvc.perform(post("/v1/api/game/universes").
                        contentType(MediaType.APPLICATION_JSON).
                        content(MAPPER.writeValueAsString(Universe.builder().build()))).
                andDo(print()).andExpect(status().isOk());
    }

    @Test
    void deleteUniverse() throws Exception {
        doNothing().when(service).deleteUniverse(anyString());
        mockMvc.perform(delete("/v1/api/game/universes/1").contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).andExpect(status().isOk());
    }

    @Test
    void startUniverse() throws Exception {
        doNothing().when(service).startUniverse(any());
        mockMvc.perform(post("/v1/api/game/universes/1/start").
                        contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).andExpect(status().isOk());
    }

    @Test
    void stopUniverse() throws Exception {
        doNothing().when(service).stopUniverse(any());
        mockMvc.perform(post("/v1/api/game/universes/1/stop").
                        contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).andExpect(status().isOk());
    }

    @Test
    void getUniverse() throws Exception {
        doReturn(null).when(service).getUniverse(anyString());
        mockMvc.perform(get("/v1/api/game/universes/1").contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).andExpect(status().isOk());
    }

    @Test
    void getUniverseList() throws Exception {
        doReturn(Collections.emptyList()).when(service).getUniversesList();
        mockMvc.perform(get("/v1/api/game/universes").contentType(MediaType.APPLICATION_JSON)).
                andDo(print()).andExpect(status().isOk());
    }
}