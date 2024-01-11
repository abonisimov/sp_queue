package net.alex.game.queue.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@ToString
@Getter
@SuperBuilder
@Jacksonized
public class GameEvent implements Delayed {
    private final String id;
    private final long delay;
    private final TimeUnit timeUnit;
    private long startTime;
    @Setter
    private long backupTime;

    public void init() {
        this.startTime = TimeUnit.MILLISECONDS.convert(System.currentTimeMillis(), timeUnit) + delay;
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
}
