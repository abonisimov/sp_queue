package net.alex.game.queue;

import net.alex.game.queue.config.ExecutorConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private ExecutorConfig executorConfig;

    @Test
    @DirtiesContext
    void testStart() {
        assertNotNull(queueService);
    }

    /*@Test
    @DirtiesContext
    void testStartStop() {
        Set<String> universeIds = Stream.iterate(1L, n -> n + 1).
                limit(executorConfig.poolSize()).
                map(String::valueOf).
                collect(Collectors.toUnmodifiableSet());

        universeIds.forEach(universeId -> queueService.startUniverse(universeId));
        universeIds.forEach(universeId -> assertTrue(queueService.isUniverseRunning(universeId)));
        assertEquals(universeIds, queueService.getRunningUniverses());

        universeIds.forEach(universeId -> queueService.stopThread(universeId));
        universeIds.forEach(universeId -> assertFalse(queueService.isUniverseRunning(universeId)));
        assertTrue(queueService.getRunningUniverses().isEmpty());
    }

    @Test
    @DirtiesContext
    void testUniverseCountExceededException() {
        assertThrows(UniverseCountExceededException.class, () ->
                Stream.iterate(1L, n -> n + 1).
                        limit(executorConfig.poolSize() + 1).
                        map(String::valueOf).
                        forEach(universeId -> queueService.startUniverse(universeId)));
    }

    @Test
    @DirtiesContext
    void testUniverseNotFoundException() {
        assertThrows(UniverseNotFoundException.class, () ->
                queueService.addEvent("-1", "1", 0, TimeUnit.MILLISECONDS));
    }

    @Test
    @DirtiesContext
    void testUniverseAlreadyRunningException() {
        assertThrows(UniverseAlreadyRunningException.class, () -> {
            queueService.startUniverse("1");
            queueService.startUniverse("1");
        });
    }*/
}