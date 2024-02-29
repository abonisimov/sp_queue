package net.alex.game.queue.serialize;

import jakarta.transaction.Transactional;
import net.alex.game.queue.persistence.entity.EventBackupEntity;
import net.alex.game.queue.persistence.repo.EventBackupCrudRepo;
import net.alex.game.queue.persistence.repo.EventBackupNativeRepo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("PostgresqlEventSerializer")
public class PostgresqlEventSerializer extends EventSerializer {


    private final EventBackupNativeRepo eventBackupNativeRepo;
    private final EventBackupCrudRepo eventBackupCrudRepo;

    public PostgresqlEventSerializer(EventBackupNativeRepo eventBackupNativeRepo,
                                     EventBackupCrudRepo eventBackupCrudRepo) {
        this.eventBackupNativeRepo = eventBackupNativeRepo;
        this.eventBackupCrudRepo = eventBackupCrudRepo;
    }

    @Override
    public List<String> readFromDataStore() {
        List<String> result = new ArrayList<>();
        List<EventBackupEntity> entities = eventBackupNativeRepo.readAndDeleteWithLock();
        while (!entities.isEmpty()) {
            result.addAll(entities.stream().map(EventBackupEntity::getEventJson).toList());
            entities = eventBackupNativeRepo.readAndDeleteWithLock();
        }
        return result;
    }

    @Override
    @Transactional
    public void writeToDataStore(List<String> events) {
        eventBackupCrudRepo.saveAll(events.stream().map(s -> new EventBackupEntity(null, s)).toList());
    }
}
