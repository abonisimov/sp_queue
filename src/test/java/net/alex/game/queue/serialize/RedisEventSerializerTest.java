package net.alex.game.queue.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.QueueApplication;
import net.alex.game.queue.RedisProperties;
import net.alex.game.queue.TestRedisConfiguration;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.GameEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {QueueApplication.class, TestRedisConfiguration.class})
@SpringBootTest(classes = TestRedisConfiguration.class)
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
    void testDeserializationGameEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(GameEvent.builder().
                id("123").delay(500).timeUnit(TimeUnit.MILLISECONDS).backupTime(900).build());
        log.debug(json);
        assertTrue(StringUtils.isNotBlank(json));
        GameEvent event = mapper.reader().readValue(json, GameEvent.class);
        log.debug("{}", event);
        assertNotNull(event);
        assertEquals("123", event.getId());
        assertEquals(500, event.getDelay());
        assertEquals(TimeUnit.MILLISECONDS, event.getTimeUnit());
        assertEquals(900, event.getBackupTime());
    }

    @Test
    void testDeserializationFastModeSwitchEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writer().writeValueAsString(FastModeSwitchEvent.builder().
                id("321").delay(800).timeUnit(TimeUnit.SECONDS).backupTime(1000).enable(true).build());
        log.debug(json);
        assertTrue(StringUtils.isNotBlank(json));
        FastModeSwitchEvent event = mapper.reader().readValue(json, FastModeSwitchEvent.class);
        log.debug("{}", event);
        assertNotNull(event);
        assertEquals("321", event.getId());
        assertEquals(800, event.getDelay());
        assertEquals(TimeUnit.SECONDS, event.getTimeUnit());
        assertEquals(1000, event.getBackupTime());
        assertTrue(event.isEnable());
    }
}