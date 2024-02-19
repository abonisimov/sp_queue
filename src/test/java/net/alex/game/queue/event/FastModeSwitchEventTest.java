package net.alex.game.queue.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class FastModeSwitchEventTest {
    @Test
    void testDeserializationFastModeSwitchEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writer().writeValueAsString(FastModeSwitchEvent.builder().universeId("1").
                id("321").delay(800).timeUnit(TimeUnit.SECONDS).backupTime(1000).enable(true).build());
        log.debug(json);
        assertTrue(StringUtils.isNotBlank(json));
        FastModeSwitchEvent event = mapper.reader().readValue(json, FastModeSwitchEvent.class);
        log.debug("{}", event);
        assertNotNull(event);
        assertEquals("1", event.getUniverseId());
        assertEquals("321", event.getId());
        assertEquals(800, event.getDelay());
        assertEquals(TimeUnit.SECONDS, event.getTimeUnit());
        assertEquals(1000, event.getBackupTime());
        assertTrue(event.isEnable());
        assertEquals(FastModeSwitchEvent.class.getName(), event.getEventClass());
    }
}