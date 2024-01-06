package net.alex.game.queue.executor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameEventRunner implements EventRunner {
    public void executeEvent(long universeId, String eventId) {
        log.info("Routing event {} for universe {}", eventId, universeId);
    }
}
