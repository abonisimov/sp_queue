package net.alex.game.queue.persistence.repo;

import net.alex.game.queue.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepo extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByNickName(String nickName);
    Page<UserEntity> findAll(Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByNickName(String nickName);
}
