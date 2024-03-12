package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.PasswordTokenEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordTokenRepo extends CrudRepository<PasswordTokenEntity, Long> {
    Optional<PasswordTokenEntity> findByToken(String token);
    Optional<PasswordTokenEntity> findByUser(UserEntity user);
    void deleteByExpiryTimeLessThan(LocalDateTime time);
}
