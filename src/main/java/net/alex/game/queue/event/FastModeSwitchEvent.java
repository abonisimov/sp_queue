package net.alex.game.queue.event;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FastModeSwitchEvent extends GameEvent {

    private final boolean enable;

    public FastModeSwitchEvent(boolean enable, long delay, TimeUnit timeUnit) {
        super(UUID.randomUUID().toString(), delay, timeUnit);
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }
}
