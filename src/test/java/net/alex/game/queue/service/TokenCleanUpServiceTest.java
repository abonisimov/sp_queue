package net.alex.game.queue.service;

import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.model.UserOut;
import net.alex.game.queue.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TokenCleanUpServiceTest  extends AbstractUserTest {

    @Autowired
    private TokenCleanUpService tokenCleanUpService;

    @Test
    void cleanUpExpiredTokens() {
        cleanUserRecords();
        UserOut userOut = registerUser();
        UserEntity userEntity = userRepo.findById(userOut.getId()).orElseThrow();

        saveAccessToken("access_token_expired", userEntity, LocalDateTime.now().minusMinutes(1));
        saveAccessToken("access_token_valid", userEntity, LocalDateTime.now().plusMinutes(1));

        saveRestorePasswordToken("password_token_expired", userEntity, LocalDateTime.now().minusMinutes(1));
        saveRestorePasswordToken("password_token_valid", userEntity, LocalDateTime.now().plusMinutes(1));

        tokenCleanUpService.cleanUpExpiredTokens();

        assertFalse(accessTokenRepo.findByToken("access_token_expired").isPresent());
        assertFalse(restorePasswordTokenRepo.findByToken("password_token_expired").isPresent());
        assertTrue(accessTokenRepo.findByToken("access_token_valid").isPresent());
        assertTrue(restorePasswordTokenRepo.findByToken("password_token_valid").isPresent());
    }
}