package net.alex.game.queue.event;

import lombok.experimental.SuperBuilder;
import net.alex.game.model.event.GameEvent;

@SuperBuilder
public class QueueTerminationEvent extends GameEvent implements SystemEvent {
}
