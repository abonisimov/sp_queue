package net.alex.game.queue.service;

import net.alex.game.queue.config.ExecutorConfig;
import net.alex.game.queue.thread.GameThreadStats;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ThreadServiceTest {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ExecutorConfig executorConfig;

    @Test
        //@DirtiesContext
    void testGetThreadStatisticsList() {
        List<GameThreadStats> result = threadService.getThreadStatisticsList();
        assertNotNull(result);
        assertEquals(executorConfig.poolSize(), result.size());
        result.forEach(Assertions::assertNotNull);
    }

    @Test
    void testGetThreadStatistics() {
        List<GameThreadStats> list = threadService.getThreadStatisticsList();
        assertNotNull(list);
        assertTrue(list.size() > 0);
        long threadId = list.stream().map(GameThreadStats::getThreadId).findAny().orElseThrow();

        GameThreadStats result = threadService.getThreadStatistics(threadId);
        assertNotNull(result);
        assertEquals(threadId, result.getThreadId());
    }

}