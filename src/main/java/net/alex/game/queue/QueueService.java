package net.alex.game.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.config.ExecutorConfig;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.GameEvent;
import net.alex.game.queue.event.UniverseQueueTerminationEvent;
import net.alex.game.queue.exception.UniverseAlreadyRunningException;
import net.alex.game.queue.exception.UniverseCountExceededException;
import net.alex.game.queue.exception.UniverseNotFoundException;
import net.alex.game.queue.exception.WaitingInterruptedException;
import net.alex.game.queue.executor.GameEventRunner;
import net.alex.game.queue.executor.GameEventThread;
import net.alex.game.queue.executor.GameThreadPoolExecutor;
import net.alex.game.queue.serialize.RedisEventSerializer;
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
    private final RedisEventSerializer redisEventSerializer;

    public QueueService(ExecutorConfig executorConfig,
                        RedisEventSerializer redisEventSerializer) {
        this.executorConfig = executorConfig;
        this.redisEventSerializer = redisEventSerializer;
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
        for (long universeId : threadPoolExecutor.getUniverseSet()) {
            stopUniverse(universeId);
        }
        threadPoolExecutor.shutdown();
        while (!threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
            log.debug("Awaiting all game universes threads to finish");
        }
    }

    public synchronized void startUniverse(long universeId) {
        if (!threadPoolExecutor.hasCapacity()) {
            log.info("Thread pool has maximum threads of {}, can't start universe {}", executorConfig.poolSize(), universeId);
            throw new UniverseCountExceededException();
        }
        if (!threadPoolExecutor.isUniversePresent(universeId)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            GameEventThread gameEventThread = new GameEventThread(universeId,
                    countDownLatch, new GameEventRunner(), redisEventSerializer);
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
                throw new WaitingInterruptedException();
            }
        } else {
            log.debug("Universe {} is already running", universeId);
            throw new UniverseAlreadyRunningException();
        }
    }

    public synchronized void stopUniverse(long universeId) {
        try {
            log.debug("Stopping universe {}", universeId);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            checkAndAddEvent(universeId, UniverseQueueTerminationEvent.
                    builder().
                    id(UUID.randomUUID().toString()).
                    delay(0).
                    timeUnit(TimeUnit.MILLISECONDS).
                    shutdownLatch(countDownLatch).build());
            countDownLatch.await();
            log.debug("Universe {} stopped", universeId);
        } catch (InterruptedException e) {
            log.warn("Waiting for universe {} to stop was interrupted", universeId);
            log.warn(e.getMessage(), e);
            throw new WaitingInterruptedException();
        }
    }

    public void enableFastMode(long universeId, Duration duration) {
        FastModeSwitchEvent enableEvent = FastModeSwitchEvent.builder().
                id(UUID.randomUUID().toString()).delay(0).timeUnit(TimeUnit.MILLISECONDS).enable(true).build();
        FastModeSwitchEvent disableEvent = FastModeSwitchEvent.builder().
                id(UUID.randomUUID().toString()).delay(duration.toMillis()).
                timeUnit(TimeUnit.MILLISECONDS).enable(false).build();
        checkAndAddEvent(universeId, enableEvent);
        checkAndAddEvent(universeId, disableEvent);
    }

    public void interruptFastMode(long universeId) {
        FastModeSwitchEvent disableEvent = FastModeSwitchEvent.builder().
                id(UUID.randomUUID().toString()).
                delay(0).
                timeUnit(TimeUnit.MILLISECONDS).
                enable(false).
                build();
        checkAndAddEvent(universeId, disableEvent);
    }

    public void addEvent(long universeId, String eventId, long delay, TimeUnit timeUnit) {
        checkAndAddEvent(universeId, GameEvent.builder().id(eventId).delay(delay).timeUnit(timeUnit).build());
    }

    public boolean isUniverseRunning(long universeId) {
        return threadPoolExecutor.isUniversePresent(universeId);
    }

    public Set<Long> getRunningUniverses() {
        return threadPoolExecutor.getUniverseSet();
    }

    private void checkAndAddEvent(long universeId, GameEvent gameEvent) {
        GameEventThread gameEventThread = threadPoolExecutor.getTask(universeId);
        if (gameEventThread != null) {
            threadPoolExecutor.getTask(universeId).addEvent(gameEvent);
        } else {
            log.debug("Universe {} is not currently running, can't add event {}", universeId, gameEvent.getId());
            throw new UniverseNotFoundException();
        }
    }
}
