package net.alex.game.queue.event;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@ToString(callSuper = true)
@Getter
@SuperBuilder
@Jacksonized
public class FastModeSwitchEvent extends GameEvent {
    private final boolean enable;
}
