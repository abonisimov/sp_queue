package net.alex.game.queue.serialize;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.GameEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class InMemoryEventSerializerTest {

    @Autowired
    private InMemoryEventSerializer inMemoryEventSerializer;

    @Test
    void readWriteTest() throws IOException {
        List<GameEvent> events = new ArrayList<>();
        GameEvent event0 = GameEvent.builder().
                id("123").delay(5000).timeUnit(TimeUnit.MILLISECONDS).build();
        GameEvent event1 = FastModeSwitchEvent.builder().
                id("321").delay(8).timeUnit(TimeUnit.SECONDS).enable(true).build();
        event0.init();
        event1.init();

        long startTime0= event0.getStartTime();
        long startTime1 = event1.getStartTime();

        events.add(event0);
        events.add(event1);
        inMemoryEventSerializer.writeEvents(1L, events.iterator());

        List<GameEvent> result = new ArrayList<>();
        inMemoryEventSerializer.readEvents(1L, result::add);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(GameEvent.class, result.get(0).getClass());
        assertEquals(FastModeSwitchEvent.class, result.get(1).getClass());
        assertNotEquals(startTime0, result.get(0).getStartTime());
        assertNotEquals(startTime1, result.get(1).getStartTime());
        assertTrue(result.get(0).getStartTime() - startTime0 <= 5000);
        assertTrue(result.get(1).getStartTime() - startTime1 <= 8000);
    }
}