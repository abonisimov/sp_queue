package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.AccessTokenEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccessTokenRepo extends CrudRepository<AccessTokenEntity, Long> {

    Optional<AccessTokenEntity> findByToken(String token);
    Optional<AccessTokenEntity> findByUser(UserEntity user);
    void deleteByExpiryDateLessThan(LocalDateTime time);
}
