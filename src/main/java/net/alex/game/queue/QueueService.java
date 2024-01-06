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
import net.alex.game.queue.executor.GameEventThread;
import net.alex.game.queue.executor.GameThreadPoolExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class QueueService {
    private GameThreadPoolExecutor threadPoolExecutor;

    private final ExecutorConfig executorConfig;

    public QueueService(ExecutorConfig executorConfig) {
        this.executorConfig = executorConfig;
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
            GameEventThread gameEventThread = new GameEventThread(universeId, countDownLatch);
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
            checkAndAddEvent(universeId, new UniverseQueueTerminationEvent(countDownLatch));
            countDownLatch.await();
            log.debug("Universe {} stopped", universeId);
        } catch (InterruptedException e) {
            log.warn("Waiting for universe {} to stop was interrupted", universeId);
            log.warn(e.getMessage(), e);
            throw new WaitingInterruptedException();
        }
    }

    public void enableFastMode(long universeId, Duration duration) {
        FastModeSwitchEvent enableEvent = new FastModeSwitchEvent(true, 0, TimeUnit.MILLISECONDS);
        FastModeSwitchEvent disableEvent = new FastModeSwitchEvent(false, duration.toMillis(), TimeUnit.MILLISECONDS);
        checkAndAddEvent(universeId, enableEvent);
        checkAndAddEvent(universeId, disableEvent);
    }

    public void interruptFastMode(long universeId) {
        FastModeSwitchEvent disableEvent = new FastModeSwitchEvent(false, 0, TimeUnit.MILLISECONDS);
        checkAndAddEvent(universeId, disableEvent);
    }

    public void addEvent(long universeId, String eventId, long delay, TimeUnit timeUnit) {
        checkAndAddEvent(universeId, new GameEvent(eventId, delay, timeUnit));
    }

    public boolean isUniverseRunning(long universeId) {
        return threadPoolExecutor.isUniversePresent(universeId);
    }

    public Set<Long> getRunningUniversesSet() {
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
