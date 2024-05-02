package net.alex.game.queue.service;

import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.exception.ResourceNotFoundException;
import net.alex.game.queue.model.out.RoleOut;
import net.alex.game.queue.model.out.UserOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.IntStream;

import static net.alex.game.queue.persistence.RoleName.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoleServiceTest extends AbstractUserTest {

    @Autowired
    private RoleService roleService;

    @BeforeEach
    void beforeEach() {
        cleanUserRecords();
    }

    @Test
    void getRoles() {
        UserOut user = registerUser();
        createOrRetrieveRoleForUser(user, ADMIN);
        createOrRetrieveRoleForUser(user, ROOT);


        Page<RoleOut> result = roleService.getRoles(PageRequest.of(0, 3, Sort.by("name")));
        assertNotNull(result);
        assertEquals(3, result.getSize());
        assertEquals(ADMIN.name(), result.toList().get(0).getName());
        assertEquals(ROOT.name(), result.toList().get(1).getName());
        assertEquals(USER.name(), result.toList().get(2).getName());
    }

    @Test
    void getUsers() {
        List<UserOut> users = IntStream.range(0, 10).mapToObj(i -> registerUniqueUser("mail" + i + "@mail.com", "nick" + i)).toList();
        RoleOut role = users.stream().findAny().map(u -> u.getRoles().stream().findAny().orElseThrow()).orElseThrow();

        Page<UserOut> result = roleService.getUsers(role.getId(), PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(users.size(), result.getSize());
        result.get().forEach(u -> assertTrue(users.contains(u)));

        result = roleService.getUsers(role.getId(), PageRequest.of(1, 5, Sort.by("email")));
        assertNotNull(result);
        assertEquals(5, result.getSize());
        result.get().forEach(u -> assertTrue(users.contains(u)));
    }

    @Test
    void getUsers_invalid_role_id() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(ResourceNotFoundException.class, () -> roleService.getUsers(Long.MIN_VALUE, pageable));
    }
}