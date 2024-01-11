package net.alex.game.queue.event;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UniverseQueueTerminationEvent extends GameEvent implements SystemEvent {

    private final CountDownLatch shutdownLatch;

    public UniverseQueueTerminationEvent(CountDownLatch shutdownLatch) {
        super(UUID.randomUUID().toString(), 0, TimeUnit.MILLISECONDS);
        this.shutdownLatch = shutdownLatch;
    }

    public CountDownLatch getShutdownLatch() {
        return shutdownLatch;
    }
}
