package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.EventBackupEntity;
import org.springframework.data.repository.CrudRepository;

public interface EventBackupCrudRepo extends CrudRepository<EventBackupEntity, Integer> {
}
