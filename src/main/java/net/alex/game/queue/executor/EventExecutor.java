package net.alex.game.queue.executor;

import net.alex.game.model.event.GameEvent;

public interface EventExecutor {
    void executeEvent(GameEvent gameEvent);
}
