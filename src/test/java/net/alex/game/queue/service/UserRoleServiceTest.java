package net.alex.game.queue.service;

import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.exception.ResourceNotFoundException;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.entity.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static net.alex.game.queue.persistence.RoleName.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRoleServiceTest extends AbstractUserTest {

    @Autowired
    private UserRoleService userRoleService;

    @BeforeEach
    void beforeEach() {
        cleanUserRecords();
    }

    @Test
    void assignRoles() {
        List<RoleIn> currentUserRoles = Collections.singletonList(new RoleIn(MEMBER.name(), 10L));
        List<RoleIn> adminRoles = Collections.singletonList(new RoleIn(ROOT.name(), null));
        List<RoleIn> rolesToAssign = Stream.of(new RoleIn(ROOT.name(), null), new RoleIn(OWNER.name(), 10L)).toList();

        UserOut userOut = createTargetAndPrincipalWithRoles(currentUserRoles, adminRoles);
        Set<RoleEntity> rolesBefore = userRepo.findById(userOut.getId()).orElseThrow().getRoles();
        assertNotNull(rolesBefore);

        userRoleService.assignRoles(userOut.getId(), rolesToAssign);
        Set<RoleEntity> rolesAfter = userRepo.findById(userOut.getId()).orElseThrow().getRoles();
        assertNotNull(rolesAfter);
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(ROOT.name())));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(ADMIN.name())));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 10L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 10L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 10L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(USER.name())));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(SYSTEM.name())));

        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(ROOT.name())).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(ADMIN.name())).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(OWNER.name())).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(MEMBER.name())).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name())).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(USER.name())).count());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(MEMBER.name())).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(MEMBER.name())).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(USER.name())).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(USER.name())).findAny().orElseThrow().getId());
    }

    @Test
    void assignRoles_invalidUser() {
        List<RoleIn> roles = Collections.singletonList(new RoleIn(MEMBER.name(), 10L));
        assertThrows(ResourceNotFoundException.class, () -> userRoleService.assignRoles(1, roles));
    }

    @Test
    void assignRoles_invalidRoles() {
        List<RoleIn> rolesToAssign = Stream.of(new RoleIn(ROOT.name(), null), new RoleIn(OWNER.name(), 10L)).toList();
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN);
        long userId = userOut.getId();
        assertThrows(AccessRestrictedException.class, () -> userRoleService.assignRoles(userId, rolesToAssign));
    }
}