package net.alex.game.queue.event;

import lombok.*;
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
    @NonNull
    private final String universeId;
    @NonNull
    private final String id;
    private final long delay;
    @NonNull
    @Builder.Default()
    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private long startTime;
    @Setter
    private long backupTime;
    @Getter(AccessLevel.NONE)
    private final String eventClass;

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
