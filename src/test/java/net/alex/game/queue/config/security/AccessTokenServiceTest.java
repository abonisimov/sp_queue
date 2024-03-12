package net.alex.game.queue.config.security;

import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.persistence.entity.AccessTokenEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccessTokenServiceTest extends AbstractUserTest {

    @Autowired
    private AccessTokenService accessTokenService;

    @BeforeEach
    void beforeEach() {
        cleanUserRecords();
    }

    @Test
    void getAuthentication() {
        String token = createTokenByRoleName("USER");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTH_TOKEN_HEADER_NAME, token);
        Optional<Authentication> authenticationOptional = accessTokenService.getAuthentication(request);
        assertTrue(authenticationOptional.isPresent());

        Authentication authentication = authenticationOptional.get();
        assertEquals(token, authentication.getCredentials());
        assertTrue(authentication.isAuthenticated());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER")));
        assertNotNull(authentication.getPrincipal());

        UserEntity userEntity = accessTokenRepo.findByToken(token).orElseThrow().getUser();
        assertNotNull(userEntity);
        assertEquals(PrincipalData.fromUserEntity(userEntity), authentication.getPrincipal());
    }

    @Test
    void getAuthentication_no_token() {
        Optional<Authentication> authentication = accessTokenService.getAuthentication(new MockHttpServletRequest());
        assertFalse(authentication.isPresent());
    }

    @Test
    void getAuthentication_expired_token() {
        String token = createTokenByRoleName("USER");
        Optional<AccessTokenEntity> tokenEntity = accessTokenRepo.findByToken(token);
        tokenEntity.orElseThrow().setExpiryDate(LocalDateTime.now().minusMinutes(1));
        accessTokenRepo.save(tokenEntity.get());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTH_TOKEN_HEADER_NAME, token);

        Optional<Authentication> authentication = accessTokenService.getAuthentication(request);
        assertFalse(authentication.isPresent());
    }

    @Test
    void getAuthentication_disabled_user() {
        String token = createTokenByRoleName("USER");
        Optional<AccessTokenEntity> tokenEntity = accessTokenRepo.findByToken(token);
        UserEntity userEntity = tokenEntity.orElseThrow().getUser();
        userEntity.setEnabled(false);
        userRepo.save(userEntity);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTH_TOKEN_HEADER_NAME, token);

        Optional<Authentication> authentication = accessTokenService.getAuthentication(request);
        assertFalse(authentication.isPresent());
    }
}