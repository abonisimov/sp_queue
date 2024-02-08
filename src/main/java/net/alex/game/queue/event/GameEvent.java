package net.alex.game.queue.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Slf4j
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
    private String eventClass;

    public void init() {
        this.startTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, timeUnit);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(startTime, ((GameEvent)o).startTime);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    public void changeDelay(long delayDiff, TimeUnit diffTimeUnit) {
        this.startTime += TimeUnit.MILLISECONDS.convert(delayDiff, diffTimeUnit);
    }

    public String getEventClass() {
        return this.getClass().getName();
    }
}
