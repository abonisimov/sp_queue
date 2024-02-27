package net.alex.game.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.config.ExecutorConfig;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.UniverseQueueTerminationEvent;
import net.alex.game.queue.exception.UniverseAlreadyRunningException;
import net.alex.game.queue.exception.UniverseCountExceededException;
import net.alex.game.queue.exception.UniverseNotFoundException;
import net.alex.game.queue.exception.WaitingInterruptedException;
import net.alex.game.queue.executor.GameEventExecutor;
import net.alex.game.queue.executor.GameEventThread;
import net.alex.game.queue.executor.GameThreadPoolExecutor;
import net.alex.game.queue.serialize.InMemoryEventSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class QueueService {
    private GameThreadPoolExecutor threadPoolExecutor;

    private final ExecutorConfig executorConfig;
    private final InMemoryEventSerializer inMemoryEventSerializer;

    public QueueService(ExecutorConfig executorConfig,
                        InMemoryEventSerializer inMemoryEventSerializer) {
        this.executorConfig = executorConfig;
        this.inMemoryEventSerializer = inMemoryEventSerializer;
    }

    @PostConstruct
    public void startUp() {
        int poolSize = executorConfig.poolSize();
        log.info("Starting thread pool of size {}", poolSize);
        threadPoolExecutor = new GameThreadPoolExecutor(
                poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        log.info("Shutting down thread pool");
        for (String universeId : threadPoolExecutor.getUniverseSet()) {
            stopUniverse(universeId);
        }
        threadPoolExecutor.shutdown();
        while (!threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
            log.debug("Awaiting all game universes threads to finish");
        }
    }

    public synchronized void startUniverse(String universeId) {
        if (!threadPoolExecutor.hasCapacity()) {
            log.info("Thread pool has maximum threads of {}, can't start universe {}", executorConfig.poolSize(), universeId);
            throw new UniverseCountExceededException();
        }
        if (!threadPoolExecutor.isUniversePresent(universeId)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            GameEventThread gameEventThread = new GameEventThread(universeId, countDownLatch, new GameEventExecutor(),
                    inMemoryEventSerializer, executorConfig.loadFactorPrecision());
            try {
                log.debug("Starting universe {}", universeId);
                threadPoolExecutor.execute(gameEventThread);
                countDownLatch.await();
                log.debug("Universe {} started", universeId);
            } catch (RejectedExecutionException e) {
                log.warn("Universe {} thread is not accepted", universeId);
                log.warn(e.getMessage(), e);
                throw new UniverseCountExceededException();
            } catch (InterruptedException e) {
                log.warn("Waiting for universe {} to start was interrupted", universeId);
                log.warn(e.getMessage(), e);
                Thread.currentThread().interrupt();
                throw new WaitingInterruptedException();
            }
        } else {
            log.debug("Universe {} is already running", universeId);
            throw new UniverseAlreadyRunningException();
        }
    }

    public synchronized void stopUniverse(String universeId) {
        try {
            log.debug("Stopping universe {}", universeId);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            checkAndAddEvent(UniverseQueueTerminationEvent.
                    builder().
                    universeId(universeId).
                    id(UUID.randomUUID().toString()).
                    delay(0).
                    timeUnit(TimeUnit.MILLISECONDS).
                    shutdownLatch(countDownLatch).build());
            countDownLatch.await();
            log.debug("Universe {} stopped", universeId);
        } catch (InterruptedException e) {
            log.warn("Waiting for universe {} to stop was interrupted", universeId);
            log.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new WaitingInterruptedException();
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

    public boolean isUniverseRunning(String universeId) {
        return threadPoolExecutor.isUniversePresent(universeId);
    }

    public Set<String> getRunningUniverses() {
        return threadPoolExecutor.getUniverseSet();
    }

    private void checkAndAddEvent(GameEvent gameEvent) {
        GameEventThread gameEventThread = threadPoolExecutor.getTask(gameEvent.getUniverseId());
        if (gameEventThread != null) {
            gameEventThread.addEvent(gameEvent);
        } else {
            log.debug("Universe {} is not currently running, can't add event {}",
                    gameEvent.getUniverseId(), gameEvent.getId());
            throw new UniverseNotFoundException();
        }
    }
}
