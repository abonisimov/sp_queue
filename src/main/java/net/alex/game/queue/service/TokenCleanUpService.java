package net.alex.game.queue.service;

import jakarta.transaction.Transactional;
import net.alex.game.queue.persistence.repo.AccessTokenRepo;
import net.alex.game.queue.persistence.repo.RestorePasswordTokenRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenCleanUpService {

    private final AccessTokenRepo accessTokenRepo;
    private final RestorePasswordTokenRepo restorePasswordTokenRepo;

    public TokenCleanUpService(AccessTokenRepo accessTokenRepo,
                               RestorePasswordTokenRepo restorePasswordTokenRepo) {
        this.accessTokenRepo = accessTokenRepo;
        this.restorePasswordTokenRepo = restorePasswordTokenRepo;
    }

    @Transactional
    @Scheduled(cron = "@midnight")
    public void cleanUpExpiredTokens() {
        accessTokenRepo.deleteByExpiryDateLessThan(LocalDateTime.now());
        restorePasswordTokenRepo.deleteByExpiryTimeLessThan(LocalDateTime.now());
    }
}
