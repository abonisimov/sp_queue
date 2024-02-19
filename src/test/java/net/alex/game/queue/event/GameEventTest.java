package net.alex.game.queue.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class GameEventTest {

    @Test
    void testDeserializationGameEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(GameEvent.builder().universeId("1").
                id("123").delay(500).timeUnit(TimeUnit.MILLISECONDS).backupTime(900).build());
        log.debug(json);
        assertTrue(StringUtils.isNotBlank(json));
        GameEvent event = mapper.reader().readValue(json, GameEvent.class);
        log.debug("{}", event);
        assertNotNull(event);
        assertEquals("1", event.getUniverseId());
        assertEquals("123", event.getId());
        assertEquals(500, event.getDelay());
        assertEquals(TimeUnit.MILLISECONDS, event.getTimeUnit());
        assertEquals(900, event.getBackupTime());
        assertEquals(GameEvent.class.getName(), event.getEventClass());
    }
}