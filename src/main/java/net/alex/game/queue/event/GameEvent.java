package net.alex.game.queue.event;

import lombok.ToString;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@ToString
public class GameEvent implements Delayed {
    private final String id;
    private final long delay;
    private long startTime;
    private final TimeUnit timeUnit;

    public GameEvent(String id, long delay, TimeUnit timeUnit) {
        this.id = id;
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    public void init() {
        this.startTime = TimeUnit.MILLISECONDS.convert(System.currentTimeMillis(), timeUnit) + delay;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(startTime, ((GameEvent)o).startTime);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - TimeUnit.MILLISECONDS.convert(System.currentTimeMillis(), timeUnit);
        return unit.convert(diff, timeUnit);
    }

    public void changeDelay(long delayDiff, TimeUnit diffTimeUnit) {
        this.startTime += TimeUnit.MILLISECONDS.convert(delayDiff, diffTimeUnit);
    }

    public long getStartTime() {
        return startTime;
    }
}
