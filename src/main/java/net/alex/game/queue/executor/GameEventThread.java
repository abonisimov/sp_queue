package net.alex.game.queue.executor;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.GameEvent;
import net.alex.game.queue.event.UniverseQueueTerminationEvent;
import net.alex.game.queue.exception.EventDeclinedException;
import net.alex.game.queue.serialize.EventSerializer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class GameEventThread implements Runnable {

    private final EventRunner eventRunner;
    private final EventSerializer eventSerializer;

    private final DelayQueue<GameEvent> eventDelayQueue = new DelayQueue<>();
    private final long universeId;
    private final CountDownLatch startupLatch;
    private final ReentrantLock lock = new ReentrantLock();

    private boolean fastMode = false;
    private long fastModeTimestamp = -1L;

    public GameEventThread(long universeId,
                           CountDownLatch startupLatch,
                           EventRunner eventRunner,
                           EventSerializer eventSerializer) {
        this.universeId = universeId;
        this.startupLatch = startupLatch;
        this.eventRunner = eventRunner;
        this.eventSerializer = eventSerializer;
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
                    eventRunner.executeEvent(universeId, event.getId());
                }
            }
        } catch (InterruptedException e) {
            log.warn("Universe {} thread was interrupted", universeId);
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
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

    private void cleanUp() {
        lock.lock();
        try {
            eventSerializer.writeEvents(universeId, eventDelayQueue.iterator());
            eventDelayQueue.clear();
            log.debug("Universe {} thread finished", universeId);
        } finally {
            lock.unlock();
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
