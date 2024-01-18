package net.alex.game.queue.serialize;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RedisEventSerializer extends EventSerializer {

    private final RedissonClient client;

    public RedisEventSerializer(RedissonClient client) {
        this.client = client;
    }

    public List<String> readFromDataWarehouse(long universeId) {
        return client.getList(String.valueOf(universeId));
    }

    public void writeToDataWarehouse(long universeId, List<String> events) {
        RList<String> list = client.getList(String.valueOf(universeId));
        list.addAll(events);
    }
}
