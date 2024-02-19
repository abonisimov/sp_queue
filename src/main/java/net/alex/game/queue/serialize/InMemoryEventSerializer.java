package net.alex.game.queue.serialize;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryEventSerializer extends EventSerializer {

    private static final Map<String, List<String>> MAP = new ConcurrentHashMap<>();

    public List<String> readFromDataWarehouse(String universeId) {
        return MAP.getOrDefault(universeId, Collections.emptyList());
    }

    public void writeToDataWarehouse(String universeId, List<String> events) {
        MAP.put(universeId, events);
    }
}
