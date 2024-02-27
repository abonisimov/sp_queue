package net.alex.game.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.config.ExecutorConfig;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.exception.EventDeclinedException;
import net.alex.game.queue.executor.GameEventThread;
import net.alex.game.queue.executor.GameThreadPoolExecutor;
import net.alex.game.queue.serialize.EventSerializer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class QueueService {
    private GameThreadPoolExecutor threadPoolExecutor;

    private final ExecutorConfig executorConfig;
    private final EventSerializer eventSerializer;

    public QueueService(ExecutorConfig executorConfig,
                        EventSerializer eventSerializer) {
        this.executorConfig = executorConfig;
        this.eventSerializer = eventSerializer;
    }

    @PostConstruct
    public void startUp() {
        int poolSize = executorConfig.poolSize();
        long loadFactorPrecision = executorConfig.loadFactorPrecision();
        log.info("Starting thread pool of size {}, load factor precision {}", poolSize, loadFactorPrecision);
        threadPoolExecutor = new GameThreadPoolExecutor(poolSize, loadFactorPrecision);
        try {
            eventSerializer.readEvents(this::checkAndAddEvent);
        } catch (IOException e) {
            log.warn("Unable to restore events from the database");
            log.warn(e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        log.info("Shutting down thread pool");
        threadPoolExecutor.shutdown(eventSerializer);
        while (!threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
            log.debug("Awaiting all game threads to finish");
        }
    }

    public void enableFastMode(String universeId, Duration duration) {
        FastModeSwitchEvent enableEvent = FastModeSwitchEvent.builder().universeId(universeId).
                id(UUID.randomUUID().toString()).delay(0).timeUnit(TimeUnit.MILLISECONDS).enable(true).build();
        FastModeSwitchEvent disableEvent = FastModeSwitchEvent.builder().universeId(universeId).
                id(UUID.randomUUID().toString()).delay(duration.toMillis()).
                timeUnit(TimeUnit.MILLISECONDS).enable(false).build();
        checkAndAddEvent(enableEvent);
        checkAndAddEvent(disableEvent);
    }

    public void interruptFastMode(String universeId) {
        FastModeSwitchEvent disableEvent = FastModeSwitchEvent.builder().
                universeId(universeId).
                id(UUID.randomUUID().toString()).
                delay(0).
                timeUnit(TimeUnit.MILLISECONDS).
                enable(false).
                build();
        checkAndAddEvent(disableEvent);
    }

    public void addEvent(String universeId, String eventId, long delay, TimeUnit timeUnit) {
        checkAndAddEvent(GameEvent.builder().universeId(universeId).id(eventId).delay(delay).timeUnit(timeUnit).build());
    }

    private void checkAndAddEvent(GameEvent gameEvent) {
        GameEventThread gameEventThread = threadPoolExecutor.getVacantThread();
        if (gameEventThread != null) {
            gameEventThread.addEvent(gameEvent);
        } else {
            log.debug("Can't access thread to pass event into the queue, universeId {}, eventId {}",
                    gameEvent.getUniverseId(), gameEvent.getId());
            throw new EventDeclinedException();
        }
    }
}
