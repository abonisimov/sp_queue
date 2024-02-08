package net.alex.game.queue.executor;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.GameEvent;

@Slf4j
public class GameEventExecutor implements EventExecutor {
    public void executeEvent(long universeId, GameEvent gameEvent) {
        log.info("Routing event {} for universe {}", gameEvent.getId(), universeId);
    }
}
