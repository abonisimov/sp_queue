package net.alex.game.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.Colony;
import net.alex.game.model.Universe;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.config.ExecutorConfig;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.exception.EventDeclinedException;
import net.alex.game.queue.exception.ThreadNotFoundException;
import net.alex.game.queue.executor.GameThreadPoolExecutor;
import net.alex.game.queue.serialize.EventSerializer;
import net.alex.game.queue.thread.GameEventThread;
import net.alex.game.queue.thread.GameThreadStats;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
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

    public GameThreadStats getThreadStatistics(long threadId) {
        return threadPoolExecutor.getThreadStatisticsList().stream().
                filter(s -> s.getThreadId() == threadId).findAny().
                orElseThrow(ThreadNotFoundException::new);
    }

    public List<GameThreadStats> getThreadStatisticsList() {
        return threadPoolExecutor.getThreadStatisticsList();
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

    public void createColony(Colony colony) {
        //todo: implement
    }

    public void deleteColony(String colonyId) {
        //todo: implement
    }

    public Colony getColony(String colonyId) {
        return null; //todo: implement
    }

    public List<Colony> getColoniesList(String universeId) {
        return Collections.emptyList(); //todo: implement
    }

    public void addEvent(GameEvent gameEvent) {
        checkAndAddEvent(gameEvent);
    }

    public void createUniverse(Universe universe) {
        //todo: implement
    }

    public void deleteUniverse(String universeId) {
        //todo: implement
    }

    public void startUniverse(String universeId) {
        //todo: implement
    }

    public void stopUniverse(String universeId) {
        //todo: implement
    }

    public Universe getUniverse(String universeId) {
        return null; //todo: implement
    }

    public List<Universe> getUniversesList() {
        return Collections.emptyList(); //todo: implement
    }
}
