package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserRepo extends CrudRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

    @Override
    void delete(UserEntity user);

}
