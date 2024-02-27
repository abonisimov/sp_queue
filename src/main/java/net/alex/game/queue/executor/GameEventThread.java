package net.alex.game.queue.executor;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.QueueTerminationEvent;
import net.alex.game.queue.exception.EventDeclinedException;
import net.alex.game.queue.serialize.EventSerializer;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class GameEventThread implements Runnable {

    private final EventExecutor eventExecutor;
    private final long loadFactorPrecision;

    private final DelayQueue<GameEvent> eventDelayQueue = new DelayQueue<>();
    private final ReentrantLock lock = new ReentrantLock();

    private final AtomicReference<GameThreadStats> statsAtomicReference = new AtomicReference<>(new GameThreadStats());

    private boolean fastMode = false;
    private long fastModeTimestamp = -1L;

    public GameEventThread(EventExecutor eventExecutor, long loadFactorPrecision) {
        this.eventExecutor = eventExecutor;
        this.loadFactorPrecision = loadFactorPrecision;
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
            log.debug("Queue thread is shutting down, event {} is declined", event.getId());
            throw new EventDeclinedException();
        }
    }

    public GameThreadStats getStatistics() {
        return statsAtomicReference.get();
    }

    @Override
    public void run() {
        CountDownLatch shutdownLatch = null;
        EventSerializer eventSerializer = null;
        try {
            long startTime = System.currentTimeMillis();
            for (;;) {
                GameEvent event = fetchEvent();
                long cycleStartTime = System.currentTimeMillis();
                logEvent(event);
                boolean result = true;
                if (event instanceof QueueTerminationEvent) {
                    eventSerializer = ((QueueTerminationEvent) event).getEventSerializer();
                    shutdownLatch = ((QueueTerminationEvent) event).getShutdownLatch();
                    break;
                } else if (event instanceof FastModeSwitchEvent) {
                    switchFastMode((FastModeSwitchEvent)event);
                } else {
                    result = eventExecutor.executeEvent(event);
                }
                updateStats(startTime, cycleStartTime, System.currentTimeMillis(), result);
            }
        } catch (InterruptedException e) {
            log.warn("Queue thread was interrupted");
            log.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        } finally {
            cleanUp(shutdownLatch, eventSerializer);
        }
    }

    private void updateStats(long startTime, long cycleStartTime, long cycleEndTime, boolean isSuccessCycle) {
        GameThreadStats newValue;
        GameThreadStats current;
        do {
            current = statsAtomicReference.get();
            newValue = GameThreadStats.updateStatsAndGet(
                    current, startTime, cycleStartTime, cycleEndTime, isSuccessCycle, loadFactorPrecision);
        } while(!statsAtomicReference.compareAndSet(current, newValue));
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

    private void cleanUp(CountDownLatch shutdownLatch, EventSerializer eventSerializer) {
        lock.lock();
        try {
            eventSerializer.writeEvents(eventDelayQueue.iterator());
        } catch (IOException e) {
            log.warn("Unable to backup queue because of the following reason:");
            log.warn(e.getMessage(), e);
        } finally {
            eventDelayQueue.clear();
            log.debug("Queue thread finished");
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
