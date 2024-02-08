package net.alex.game.queue.executor;

import net.alex.game.queue.event.GameEvent;

public interface EventExecutor {
    void executeEvent(long universeId, GameEvent gameEvent);
}
