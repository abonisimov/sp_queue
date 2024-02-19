package net.alex.game.queue.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEventJSONTest {

    @Test
    void readWriteTest() throws IOException, ClassNotFoundException {
        FastModeSwitchEvent event = FastModeSwitchEvent.builder().universeId("1").id("2").build();
        String json = GameEventJSON.toJSON(event);
        GameEvent result = GameEventJSON.fromJSON(json);

        assertNotNull(result);
        assertTrue(result instanceof FastModeSwitchEvent);
    }
}