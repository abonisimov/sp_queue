package net.alex.game.queue.serialize;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("InMemoryEventSerializer")
public class InMemoryEventSerializer extends EventSerializer {

    private static final List<String> STORE = Collections.synchronizedList(new ArrayList<>());

    public List<String> readFromDataStore() {
        return Collections.unmodifiableList(STORE);
    }

    public void writeToDataStore(List<String> events) {
        STORE.clear();
        STORE.addAll(events);
    }
}
