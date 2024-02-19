package net.alex.game.queue.executor;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.GameEvent;
import net.alex.game.queue.event.UniverseQueueTerminationEvent;
import net.alex.game.queue.exception.EventDeclinedException;
import net.alex.game.queue.serialize.EventSerializer;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class GameEventThread implements Runnable {

    private final EventExecutor eventExecutor;
    private final EventSerializer eventSerializer;

    private final DelayQueue<GameEvent> eventDelayQueue = new DelayQueue<>();
    private final String universeId;
    private final CountDownLatch startupLatch;
    private final ReentrantLock lock = new ReentrantLock();

    private boolean fastMode = false;
    private long fastModeTimestamp = -1L;

    public GameEventThread(String universeId,
                           CountDownLatch startupLatch,
                           EventExecutor eventExecutor,
                           EventSerializer eventSerializer) {
        this.universeId = universeId;
        this.startupLatch = startupLatch;
        this.eventExecutor = eventExecutor;
        this.eventSerializer = eventSerializer;
    }

    public String getUniverseId() {
        return universeId;
    }

    public void addEvent(GameEvent event) {
        boolean lockAcquired = lock.tryLock();
        if (lockAcquired) {
            try {
                if (event.getUniverseId().equals(universeId)) {
                    event.init();
                    eventDelayQueue.offer(event);
                } else {
                    log.debug("Thread for universe {} can't process event from universe {} with id {}",
                            universeId, event.getUniverseId(), event.getId());
                    throw new EventDeclinedException();
                }
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
            eventSerializer.readEvents(universeId, this::addEvent);
            for (;;) {
                GameEvent event = fetchEvent();
                logEvent(event);
                if (event instanceof UniverseQueueTerminationEvent) {
                    shutdownLatch = ((UniverseQueueTerminationEvent) event).getShutdownLatch();
                    break;
                } else if (event instanceof FastModeSwitchEvent) {
                    switchFastMode((FastModeSwitchEvent)event);
                } else {
                    eventExecutor.executeEvent(event);
                }
            }
        } catch (InterruptedException e) {
            log.warn("Universe {} thread was interrupted", universeId);
            log.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        } finally {
            cleanUp(shutdownLatch);
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
                    //noinspection ResultOfMethodCallIgnored
                    eventDelayQueue.remove(gameEvent);
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
        if (event.isEnable() && fastModeTimestamp == -1) {
            fastModeTimestamp = event.getStartTime();
        } else if (!event.isEnable() && fastModeTimestamp != -1) {
            long diff = event.getStartTime() - fastModeTimestamp;
            eventDelayQueue.forEach(e -> e.changeDelay(-diff, TimeUnit.MILLISECONDS));
            fastModeTimestamp = -1;
        }
        fastMode = event.isEnable();
    }

    private void cleanUp(CountDownLatch shutdownLatch) {
        lock.lock();
        try {
            eventSerializer.writeEvents(universeId, eventDelayQueue.iterator());
        } catch (IOException e) {
            log.warn("Unable to backup queue for universe {} because of the following reason:", universeId);
            log.warn(e.getMessage(), e);
        } finally {
            eventDelayQueue.clear();
            log.debug("Universe {} thread finished", universeId);
            lock.unlock();
            if (shutdownLatch != null) {
                shutdownLatch.countDown();
            }
        }
    }

    private void logEvent(GameEvent event) {
        log.trace("Event {} with id {} fetched with delay {} ms, fast mode - {}",
                event.getClass().getSimpleName(),
                event.getId(),
                event.getDelay(TimeUnit.MILLISECONDS),
                fastMode);
    }
}
