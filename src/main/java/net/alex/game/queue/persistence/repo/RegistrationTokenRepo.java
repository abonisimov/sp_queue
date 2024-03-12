package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.RegistrationTokenEntity;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RegistrationTokenRepo extends CrudRepository<RegistrationTokenEntity, Long> {
    Optional<RegistrationTokenEntity> findByToken(String token);
    Optional<RegistrationTokenEntity> findByEmail(String email);
    void deleteByExpiryTimeLessThan(LocalDateTime time);
}
