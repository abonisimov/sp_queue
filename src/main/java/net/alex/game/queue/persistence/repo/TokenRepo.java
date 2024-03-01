package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.TokenEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.stream.Stream;

public interface TokenRepo extends CrudRepository<TokenEntity, Long> {

    TokenEntity findByToken(String token);

    TokenEntity findByUser(UserEntity user);

    Stream<TokenEntity> findAllByExpiryDateLessThan(Date now);

    void deleteByExpiryDateLessThan(Date now);

    @Modifying
    @Query("delete from TokenEntity t where t.expiryDate <= ?1")
    void deleteAllExpiredSince(Date now);
}
