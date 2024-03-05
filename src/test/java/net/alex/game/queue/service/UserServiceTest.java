package net.alex.game.queue.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.exception.InvalidCredentialsException;
import net.alex.game.queue.model.CredentialsIn;
import net.alex.game.queue.model.UserIn;
import net.alex.game.queue.model.UserOut;
import net.alex.game.queue.persistence.entity.TokenEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import net.alex.game.queue.persistence.repo.RoleRepo;
import net.alex.game.queue.persistence.repo.TokenRepo;
import net.alex.game.queue.persistence.repo.UserRepo;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private TokenRepo tokenRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @Transactional
    void beforeEach() {
        tokenRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();
    }

    @Test
    void register() {
        UserOut result = service.register(UserIn.builder()
                .firstName("Alex")
                .lastName("Test")
                .nickName("Nick")
                .password("SomeStrongPass1!")
                .matchingPassword("SomeStrongPass1!")
                .email("test@test.com")
                .build());

        assertNotNull(result);
        assertEquals("Nick", result.getNickName());
        assertEquals("Alex", result.getFirstName());
        assertEquals("Test", result.getLastName());
        assertEquals("test@test.com", result.getEmail());
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertEquals("USER", result.getRoles().get(0).getName());

        UserEntity userEntity = new UserEntity();
        userEntity.setId(result.getId());
        Optional<TokenEntity> tokenEntity = tokenRepo.findByUser(userEntity);
        assertTrue(tokenEntity.isPresent());
    }

    @Test
    void login() {
        UserOut userOut = service.register(UserIn.builder()
                .firstName("Alex")
                .lastName("Test")
                .nickName("Nick")
                .password("SomeStrongPass1!")
                .matchingPassword("SomeStrongPass1!")
                .email("test@test.com")
                .build());

        CredentialsIn credentials = CredentialsIn.builder().nickName("Nick").password("SomeStrongPass1!").build();
        HttpServletResponse response = new MockHttpServletResponse();
        UserOut result = service.login(credentials, response);
        assertNotNull(result);
        assertEquals("Nick", result.getNickName());
        assertEquals("Alex", result.getFirstName());
        assertEquals("Test", result.getLastName());
        assertEquals("test@test.com", result.getEmail());
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertEquals("USER", result.getRoles().get(0).getName());
        assertNotNull(result.getLastLogin());
        assertTrue(response.containsHeader(AUTH_TOKEN_HEADER_NAME));

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userOut.getId());
        Optional<TokenEntity> tokenEntity = tokenRepo.findByUser(userEntity);
        assertTrue(tokenEntity.isPresent());
        assertTrue(StringUtils.isNotBlank(tokenEntity.get().getToken()));
        assertEquals(tokenEntity.get().getToken(), response.getHeader(AUTH_TOKEN_HEADER_NAME));
    }

    @Test
    @Transactional
    void login_invalidUser() {
        CredentialsIn credentials = CredentialsIn.builder().nickName("nick").password("pass").build();
        HttpServletResponse response = new MockHttpServletResponse();
        assertThrows(InvalidCredentialsException.class, () -> service.login(credentials, response));
    }

    @Test
    @Transactional
    void login_invalidPassword() {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("Alex");
        userEntity.setLastName("Test");
        userEntity.setNickName("Nick");
        userEntity.setEmail("mail@mail.com");
        userEntity.setEnabled(true);
        userEntity.setPassword(passwordEncoder.encode("pass"));
        userRepo.save(userEntity);

        CredentialsIn credentials = CredentialsIn.builder().nickName("Nick").password("pass1").build();
        HttpServletResponse response = new MockHttpServletResponse();
        assertThrows(InvalidCredentialsException.class, () -> service.login(credentials, response));
    }

    @Test
    @Transactional
    void login_disabledUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("Alex");
        userEntity.setLastName("Test");
        userEntity.setNickName("Nick");
        userEntity.setEmail("mail@mail.com");
        userEntity.setEnabled(false);
        userEntity.setPassword(passwordEncoder.encode("pass"));
        userRepo.save(userEntity);

        CredentialsIn credentials = CredentialsIn.builder().nickName("Nick").password("pass").build();
        HttpServletResponse response = new MockHttpServletResponse();
        assertThrows(AccessRestrictedException.class, () -> service.login(credentials, response));
    }

    @Test
    void getUserRole() {
    }
}