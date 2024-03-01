package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepo extends CrudRepository<RoleEntity, Long> {

    RoleEntity findByName(String name);

    @Override
    void delete(RoleEntity role);

}
