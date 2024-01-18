package net.alex.game.queue;

import net.alex.game.queue.config.ExecutorConfig;
import net.alex.game.queue.exception.UniverseAlreadyRunningException;
import net.alex.game.queue.exception.UniverseCountExceededException;
import net.alex.game.queue.exception.UniverseNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private ExecutorConfig executorConfig;

    @Test
    @DirtiesContext
    void testStartStop() {
        Set<Long> universeIds = Stream.iterate(1L, n -> n + 1).limit(executorConfig.poolSize()).
                collect(Collectors.toUnmodifiableSet());

        universeIds.forEach(universeId -> queueService.startUniverse(universeId));
        universeIds.forEach(universeId -> assertTrue(queueService.isUniverseRunning(universeId)));
        assertEquals(universeIds, queueService.getRunningUniverses());

        universeIds.forEach(universeId -> queueService.stopUniverse(universeId));
        universeIds.forEach(universeId -> assertFalse(queueService.isUniverseRunning(universeId)));
        assertTrue(queueService.getRunningUniverses().isEmpty());
    }

    @Test
    @DirtiesContext
    void testUniverseCountExceededException() {
        assertThrows(UniverseCountExceededException.class, () ->
                Stream.iterate(1L, n -> n + 1).limit(executorConfig.poolSize() + 1).
                        forEach(universeId -> queueService.startUniverse(universeId)));
    }

    @Test
    @DirtiesContext
    void testUniverseNotFoundException() {
        assertThrows(UniverseNotFoundException.class, () ->
                queueService.addEvent(-1, "1", 0, TimeUnit.MILLISECONDS));
    }

    @Test
    @DirtiesContext
    void testUniverseAlreadyRunningException() {
        queueService.startUniverse(1);
        assertThrows(UniverseAlreadyRunningException.class, () -> queueService.startUniverse(1));
    }
}