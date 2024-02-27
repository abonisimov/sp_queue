package net.alex.game.queue.executor;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;

@Slf4j
public class GameEventExecutor implements EventExecutor {
    public boolean executeEvent(GameEvent gameEvent) {
        log.info("Routing event {} for universe {}", gameEvent.getId(), gameEvent.getUniverseId());
        return true;
    }
}
