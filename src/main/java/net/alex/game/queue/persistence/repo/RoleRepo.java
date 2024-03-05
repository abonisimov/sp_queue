package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepo extends CrudRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
