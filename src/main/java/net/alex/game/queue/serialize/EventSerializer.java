package net.alex.game.queue.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.GameEvent;
import net.alex.game.queue.event.SystemEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public abstract class EventSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void readEvents(long universeId, Consumer<GameEvent> consumer) throws IOException {
        log.debug("Reading from redis queue for universe {}", universeId);
        List<String> eventJsonList = readFromDataWarehouse(universeId);
        for (String json : eventJsonList) {
            Class<GameEvent> gameEventClass = getEventClass();
            GameEvent event = MAPPER.reader().readValue(json, gameEventClass);
            changeDelay(event);
            consumer.accept(event);
        }
    }

    public void writeEvents(long universeId, Iterator<GameEvent> iterator) throws IOException {
        log.debug("Saving to redis queue for universe {}", universeId);
        long currentTimeMillis = System.currentTimeMillis();
        List<String> eventJson = new ArrayList<>();
        while (iterator.hasNext()) {
            GameEvent event = iterator.next();
            if (event instanceof SystemEvent) {
                continue;
            }
            event.setBackupTime(currentTimeMillis);
            String json = MAPPER.writer().writeValueAsString(event);
            eventJson.add(json);
        }
        writeToDataWarehouse(universeId, eventJson);
    }

    private Class<GameEvent> getEventClass() {
        return null;
    }

    private void changeDelay(GameEvent event) {
        long diff = -(event.getBackupTime() - event.getStartTime());
        event.changeDelay(diff, TimeUnit.MILLISECONDS);
    }

    public abstract List<String> readFromDataWarehouse(long universeId) throws IOException;
    public abstract void writeToDataWarehouse(long universeId, List<String> events) throws IOException;
}
