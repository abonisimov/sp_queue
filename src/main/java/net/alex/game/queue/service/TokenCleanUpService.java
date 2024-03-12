package net.alex.game.queue.service;

import jakarta.transaction.Transactional;
import net.alex.game.queue.persistence.repo.AccessTokenRepo;
import net.alex.game.queue.persistence.repo.PasswordTokenRepo;
import net.alex.game.queue.persistence.repo.RegistrationTokenRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenCleanUpService {

    private final AccessTokenRepo accessTokenRepo;
    private final PasswordTokenRepo passwordTokenRepo;
    private final RegistrationTokenRepo registrationTokenRepo;

    public TokenCleanUpService(AccessTokenRepo accessTokenRepo,
                               PasswordTokenRepo passwordTokenRepo,
                               RegistrationTokenRepo registrationTokenRepo) {
        this.accessTokenRepo = accessTokenRepo;
        this.passwordTokenRepo = passwordTokenRepo;
        this.registrationTokenRepo = registrationTokenRepo;
    }

    @Transactional
    @Scheduled(cron = "@midnight")
    public void cleanUpExpiredTokens() {
        accessTokenRepo.deleteByExpiryDateLessThan(LocalDateTime.now());
        passwordTokenRepo.deleteByExpiryTimeLessThan(LocalDateTime.now());
        registrationTokenRepo.deleteByExpiryTimeLessThan(LocalDateTime.now());
    }
}
