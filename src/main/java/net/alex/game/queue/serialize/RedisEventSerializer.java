package net.alex.game.queue.serialize;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.GameEvent;
import net.alex.game.queue.event.SystemEvent;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.function.Consumer;

@Slf4j
@Component
public class RedisEventSerializer implements EventSerializer {
    @Override
    public void readEvents(long universeId, Consumer<GameEvent> supplier) {
        log.debug("Checking if queue was already launched earlier for universe {}", universeId);
    }

    @Override
    public void writeEvents(long universeId, Iterator<GameEvent> iterator) {
        log.debug("Saving rest of the queue for universe {}", universeId);
        while (iterator.hasNext()) {
            GameEvent event = iterator.next();
            if (event instanceof SystemEvent) {
                continue;
            }
        }
    }
}
