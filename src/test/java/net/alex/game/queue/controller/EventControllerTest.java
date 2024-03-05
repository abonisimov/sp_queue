package net.alex.game.queue.controller;

import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static net.alex.game.model.event.GameEventJSON.toJSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventService service;

    @Test
    void addEvent() throws Exception {
        doNothing().when(service).addEvent(any());
        mockMvc.perform(post("/v1/api/game/events").
                        contentType(MediaType.APPLICATION_JSON).
                        content(toJSON(GameEvent.builder().id("1").build()))).
                andDo(print()).
                andExpect(status().isOk());
    }
}