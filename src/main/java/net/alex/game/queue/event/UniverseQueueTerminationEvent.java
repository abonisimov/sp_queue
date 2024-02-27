package net.alex.game.queue.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.alex.game.model.event.GameEvent;

import java.util.concurrent.CountDownLatch;

@Getter
@SuperBuilder
public class UniverseQueueTerminationEvent extends GameEvent implements SystemEvent {
    private final CountDownLatch shutdownLatch;
}
