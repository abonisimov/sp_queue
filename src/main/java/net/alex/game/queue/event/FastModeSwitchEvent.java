package net.alex.game.queue.event;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.alex.game.model.event.GameEvent;

@ToString(callSuper = true)
@Getter
@SuperBuilder
@Jacksonized
public class FastModeSwitchEvent extends GameEvent implements SystemEvent {
    private final boolean enable;
}
