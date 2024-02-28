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

    public void readEvents(Consumer<GameEvent> consumer) throws IOException {
        log.info("Reading events to queue");
        List<String> eventJsonList = readFromDataStore();
        for (String json : eventJsonList) {
            GameEvent event;
            try {
                event = GameEventJSON.fromJSON(json);
                changeDelay(event);
                consumer.accept(event);
            } catch (ClassNotFoundException e) {
                log.warn("Can't deserialize GameEvent JSON into a valid object", e);
            }
        }
    }

    public void writeEvents(Iterator<GameEvent> iterator) throws IOException {
        log.info("Writing events from queue");
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
        writeToDataStore(events);
    }

    private void changeDelay(GameEvent event) {
        long diff = -(event.getBackupTime() - event.getStartTime());
        event.changeDelay(diff, TimeUnit.MILLISECONDS);
    }

    public abstract List<String> readFromDataStore() throws IOException;
    public abstract void writeToDataStore(List<String> events) throws IOException;
}
