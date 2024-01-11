package net.alex.game.queue.event;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.concurrent.TimeUnit;

@ToString(callSuper = true)
@Getter
@SuperBuilder
@Jacksonized
public class FastModeSwitchEvent extends GameEvent {

    private final boolean enable;

    public FastModeSwitchEvent(String id, long delay, TimeUnit timeUnit, boolean enable) {
        super(id, delay, timeUnit);
        this.enable = enable;
    }
}
