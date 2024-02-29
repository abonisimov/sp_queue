package net.alex.game.queue.persistence.repo;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import net.alex.game.queue.config.ExecutorConfig;
import net.alex.game.queue.persistence.entity.EventBackupEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class EventBackupNativeRepo {

    @PersistenceContext
    private EntityManager entityManager;

    private final ExecutorConfig executorConfig;

    public EventBackupNativeRepo(ExecutorConfig executorConfig) {
        this.executorConfig = executorConfig;
    }

    @Transactional
    public List<EventBackupEntity> readAndDeleteWithLock() {
        try {
            List<EventBackupEntity> result = new ArrayList<>();
            entityManager.createNativeQuery("BEGIN WORK; LOCK TABLE events_backup IN ACCESS EXCLUSIVE MODE").executeUpdate();
            Query query = entityManager.createNativeQuery("SELECT * FROM events_backup LIMIT :num", EventBackupEntity.class);
            List<?> queryResult = query.setParameter("num", executorConfig.poolSize()).getResultList();
            for (Object object : queryResult) {
                EventBackupEntity entity = (EventBackupEntity)object;
                entityManager.createNativeQuery("DELETE FROM events_backup WHERE id = :id").
                        setParameter("id", entity.getId()).executeUpdate();
                result.add(entity);
            }
            entityManager.flush();
            return result;
        } finally {
            entityManager.createNativeQuery("COMMIT WORK").executeUpdate();
        }
    }

    @PostConstruct
    public void postConstruct() {
        Objects.requireNonNull(entityManager);
    }
}
