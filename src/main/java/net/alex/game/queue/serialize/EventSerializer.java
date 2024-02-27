package net.alex.game.queue.serialize;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;
import net.alex.game.model.event.GameEventJSON;
import net.alex.game.queue.event.SystemEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public abstract class EventSerializer {

    public void readEvents(String universeId, Consumer<GameEvent> consumer) throws IOException {
        log.debug("Reading queue for universe {}", universeId);
        List<String> eventJsonList = readFromDataWarehouse(universeId);
        for (String json : eventJsonList) {
            GameEvent event;
            try {
                event = GameEventJSON.fromJSON(json);
                changeDelay(event);
                consumer.accept(event);
            } catch (ClassNotFoundException e) {
                log.warn("Can't deserialize GameEvent JSON into a valid object, universeId = " + universeId, e);
            }
        }
    }

    public void writeEvents(String universeId, Iterator<GameEvent> iterator) throws IOException {
        log.debug("Writing queue for universe {}", universeId);
        long currentTimeMillis = System.currentTimeMillis();
        List<String> events = new ArrayList<>();
        while (iterator.hasNext()) {
            GameEvent event = iterator.next();
            if (event instanceof SystemEvent) {
                continue;
            }
            event.setBackupTime(currentTimeMillis);
            String json = GameEventJSON.toJSON(event);
            events.add(json);
        }
        writeToDataWarehouse(universeId, events);
    }

    private void changeDelay(GameEvent event) {
        long diff = -(event.getBackupTime() - event.getStartTime());
        event.changeDelay(diff, TimeUnit.MILLISECONDS);
    }

    public abstract List<String> readFromDataWarehouse(String universeId) throws IOException;
    public abstract void writeToDataWarehouse(String universeId, List<String> events) throws IOException;
}
