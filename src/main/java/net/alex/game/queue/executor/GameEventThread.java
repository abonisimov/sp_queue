package net.alex.game.queue.executor;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.GameEvent;
import net.alex.game.queue.event.UniverseQueueTerminationEvent;
import net.alex.game.queue.exception.EventDeclinedException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class GameEventThread implements Runnable {

    private final EventRunner eventRunner;
    private final DelayQueue<GameEvent> eventDelayQueue = new DelayQueue<>();
    private final long universeId;
    private final CountDownLatch startupLatch;
    private final ReentrantLock lock = new ReentrantLock();

    private boolean fastMode = false;

    public GameEventThread(long universeId, CountDownLatch startupLatch) {
        this.universeId = universeId;
        this.startupLatch = startupLatch;
        this.eventRunner = new GameEventRunner();
    }

    public GameEventThread(long universeId, CountDownLatch startupLatch, EventRunner eventRunner) {
        this.universeId = universeId;
        this.startupLatch = startupLatch;
        this.eventRunner = eventRunner;
    }

    public long getUniverseId() {
        return universeId;
    }

    public void addEvent(GameEvent event) {
        boolean lockAcquired = lock.tryLock();
        if (lockAcquired) {
            try {
                event.init();
                eventDelayQueue.offer(event);
            } finally {
                lock.unlock();
            }
        } else {
            log.debug("Universe {} is shutting down, event {} is declined", universeId, event.getId());
            throw new EventDeclinedException();
        }
    }

    @Override
    public void run() {
        CountDownLatch shutdownLatch = null;
        try {
            startupLatch.countDown();
            for (;;) {
                GameEvent event = fetchEvent();
                logEvent(event);
                if (event instanceof UniverseQueueTerminationEvent) {
                    shutdownLatch = ((UniverseQueueTerminationEvent) event).getShutdownLatch();
                    break;
                } else if (event instanceof FastModeSwitchEvent) {
                    switchFastMode((FastModeSwitchEvent)event);
                } else {
                    eventRunner.executeEvent(universeId, event.getId());
                }
            }
        } catch (InterruptedException e) {
            log.warn("Universe {} thread was interrupted", universeId);
            log.warn(e.getMessage(), e);
        } finally {
            cleanUp();
            if (shutdownLatch != null) {
                shutdownLatch.countDown();
            }
        }
    }

    private GameEvent fetchEvent() throws InterruptedException {
        if (fastMode) {
            GameEvent gameEvent;
            if (eventDelayQueue.isEmpty()) {
                gameEvent = eventDelayQueue.take();
            } else {
                gameEvent = eventDelayQueue.peek();
                if (gameEvent != null) {
                    eventDelayQueue.remove(gameEvent);// todo: check
                } else {
                    gameEvent = eventDelayQueue.take();
                }
            }
            return gameEvent;
        } else {
            return eventDelayQueue.take();
        }
    }

    private void switchFastMode(FastModeSwitchEvent event) {
        fastMode = event.isEnable();
    }

    private void cleanUp() {
        lock.lock();
        try {
            if (eventDelayQueue.isEmpty()) {
                log.debug("Queue is empty, nothing to backup");
            } else {
                queueBackup();
            }
            log.debug("Universe {} thread finished", universeId);
        } finally {
            lock.unlock();
        }
    }

    private void queueBackup() {
        log.debug("Saving rest of the queue for universe {}", universeId);
        //todo: implement
        //eventDelayQueue.forEach(e -> System.out.println("Event " + e.getId() + " cancelled in thread " + Thread.currentThread().getId()));
    }

    private void logEvent(GameEvent event) {
        log.trace("Event {} with id {} fetched with delay {} ms, fast mode - {}",
                event.getClass().getSimpleName(),
                event.getId(),
                event.getDelay(TimeUnit.MILLISECONDS),
                fastMode);
    }
}
