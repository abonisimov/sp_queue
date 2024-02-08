package net.alex.game.queue.serialize;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryEventSerializer extends EventSerializer {

    private static final Map<Long, List<String>> MAP = new ConcurrentHashMap<>();

    public List<String> readFromDataWarehouse(long universeId) {
        return MAP.getOrDefault(universeId, Collections.emptyList());
    }

    public void writeToDataWarehouse(long universeId, List<String> events) {
        MAP.put(universeId, events);
    }
}
