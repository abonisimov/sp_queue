package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.RestorePasswordTokenEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RestorePasswordTokenRepo extends CrudRepository<RestorePasswordTokenEntity, Long> {
    Optional<RestorePasswordTokenEntity> findByToken(String token);
    Optional<RestorePasswordTokenEntity> findByUser(UserEntity user);
    void deleteByExpiryTimeLessThan(LocalDateTime time);
}
