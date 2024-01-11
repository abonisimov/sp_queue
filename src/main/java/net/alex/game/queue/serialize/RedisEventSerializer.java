package net.alex.game.queue.serialize;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RedisEventSerializer extends EventSerializer {

    public List<String> readFromDataWarehouse(long universeId) throws IOException {
        //todo: read from redis
        return Collections.emptyList();
    }

    public void writeToDataWarehouse(long universeId, String json) throws IOException {
        //todo: write to redis
    }
}
