package net.alex.game.queue.thread;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.QueueTerminationEvent;
import net.alex.game.queue.event.SystemEvent;
import net.alex.game.queue.executor.EventExecutor;

import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class GameEventThread implements Runnable {

    private final EventExecutor eventExecutor;
    private final long loadFactorPrecision;

    private final DelayQueue<GameEvent> eventDelayQueue = new DelayQueue<>();

    private final AtomicReference<GameThreadStats> statsAtomicReference = new AtomicReference<>(new GameThreadStats());

    private boolean fastMode = false;
    private long fastModeTimestamp = -1L;

    public GameEventThread(EventExecutor eventExecutor, long loadFactorPrecision) {
        this.eventExecutor = eventExecutor;
        this.loadFactorPrecision = loadFactorPrecision;
    }

    public void addEvent(GameEvent event) {
        event.init();
        eventDelayQueue.offer(event);
    }

    public GameThreadStats getStatistics() {
        return statsAtomicReference.get();
    }

    public Iterator<GameEvent> getQueueIterator() {
        return eventDelayQueue.iterator();
    }

    @Override
    public void run() {
        try {
            long startTime = System.currentTimeMillis();

            for (;;) {
                GameEvent event = fetchEvent();
                long cycleStartTime = System.currentTimeMillis();
                logEvent(event);
                boolean result = true;

                if (event instanceof SystemEvent) {
                    if (event instanceof QueueTerminationEvent) {
                        break;
                    } else if (event instanceof FastModeSwitchEvent fastModeSwitchEvent) {
                        switchFastMode(fastModeSwitchEvent);
                    }
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

    private void logEvent(GameEvent event) {
        log.trace("Event {} with id {} fetched with delay {} ms, fast mode - {}",
                event.getClass().getSimpleName(),
                event.getId(),
                event.getDelay(TimeUnit.MILLISECONDS),
                fastMode);
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

    private void updateStats(long startTime, long cycleStartTime, long cycleEndTime, boolean isSuccessCycle) {
        GameThreadStats newValue;
        GameThreadStats current;
        do {
            current = statsAtomicReference.get();
            newValue = GameThreadStats.updateStatsAndGet(current, Thread.currentThread().getId(), startTime,
                    cycleStartTime, cycleEndTime, isSuccessCycle, loadFactorPrecision);
        } while(!statsAtomicReference.compareAndSet(current, newValue));
    }
}
