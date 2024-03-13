package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepo extends CrudRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
    Optional<RoleEntity> findByNameAndResourceId(String name, Long resourceId);
    Page<RoleEntity> findAll(Pageable pageable);
}
