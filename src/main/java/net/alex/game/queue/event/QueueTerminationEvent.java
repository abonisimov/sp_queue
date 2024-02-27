package net.alex.game.queue.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.serialize.EventSerializer;

import java.util.concurrent.CountDownLatch;

@Getter
@SuperBuilder
public class QueueTerminationEvent extends GameEvent implements SystemEvent {
    private final CountDownLatch shutdownLatch;
    private final EventSerializer eventSerializer;
}
