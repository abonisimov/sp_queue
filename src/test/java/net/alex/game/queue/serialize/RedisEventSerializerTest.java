package net.alex.game.queue.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.GameEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class RedisEventSerializerTest {

    @Autowired
    private RedisEventSerializer redisEventSerializer;

    @Test
    void readEvents() {
    }

    @Test
    void writeEvents() {
    }

    @Test
    void testDeserializationGameEvent() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new GameEvent("123", 500, TimeUnit.MILLISECONDS));
        log.debug(json);
        assertTrue(StringUtils.isNotBlank(json));
        GameEvent event = mapper.readValue(json, GameEvent.class);
        log.debug("{}", event);
        assertNotNull(event);
    }

    @Test
    void testDeserializationFastModeSwitchEvent() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new FastModeSwitchEvent("321", 800, TimeUnit.SECONDS, true));
        log.debug(json);
        assertTrue(StringUtils.isNotBlank(json));
        FastModeSwitchEvent event = mapper.readValue(json, FastModeSwitchEvent.class);
        log.debug("{}", event);
        assertNotNull(event);
    }
}