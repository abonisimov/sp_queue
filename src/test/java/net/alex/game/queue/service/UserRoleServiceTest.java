package net.alex.game.queue.service;

import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.exception.ResourceNotFoundException;
import net.alex.game.queue.exception.RootRequestDeclinedException;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.RoleName;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
        List<RoleIn> currentUserRoles = Stream.of(new RoleIn(MEMBER.name(), 10L), new RoleIn(WATCHER.name(), 11L)).toList();
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
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 11L));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 11L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 11L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(USER.name())));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(SYSTEM.name())));

        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(ROOT.name())).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(ADMIN.name())).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(OWNER.name())).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(MEMBER.name())).count());
        assertEquals(2, rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name())).count());
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

    @Test
    void unassignRoles() {
        List<RoleIn> currentUserRoles = Stream.of(
                        new RoleIn(WATCHER.name(), 10L),
                        new RoleIn(MEMBER.name(), 10L),
                        new RoleIn(OWNER.name(), 10L),
                        new RoleIn(WATCHER.name(), 11L),
                        new RoleIn(MEMBER.name(), 11L),
                        new RoleIn(OWNER.name(), 11L)).
                toList();
        List<RoleIn> adminRoles = Collections.singletonList(new RoleIn(ROOT.name(), null));
        List<RoleIn> rolesToUnassign = Collections.singletonList(new RoleIn(MEMBER.name(), 10L));

        UserOut userOut = createTargetAndPrincipalWithRoles(currentUserRoles, adminRoles);
        Set<RoleEntity> rolesBefore = userRepo.findById(userOut.getId()).orElseThrow().getRoles();
        assertNotNull(rolesBefore);

        userRoleService.unassignRoles(userOut.getId(), rolesToUnassign);
        Set<RoleEntity> rolesAfter = userRepo.findById(userOut.getId()).orElseThrow().getRoles();
        assertNotNull(rolesAfter);
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(ROOT.name())));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(ADMIN.name())));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 10L));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 10L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 10L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 11L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 11L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 11L));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(USER.name())));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(SYSTEM.name())));

        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 10).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 11).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 11).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 11).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(USER.name())).count());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 10).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 10).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 11).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 11).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 11).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 11).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 11).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 11).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(USER.name())).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(USER.name())).findAny().orElseThrow().getId());
    }

    @Test
    void unassignRoles_invalidUser() {
        List<RoleIn> roles = Collections.singletonList(new RoleIn(MEMBER.name(), 10L));
        assertThrows(ResourceNotFoundException.class, () -> userRoleService.unassignRoles(1, roles));
    }

    @Test
    void unassignRoles_invalidRoles() {
        List<RoleIn> rolesToAssign = Stream.of(new RoleIn(ROOT.name(), null), new RoleIn(OWNER.name(), 10L)).toList();
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN);
        long userId = userOut.getId();
        assertThrows(AccessRestrictedException.class, () -> userRoleService.unassignRoles(userId, rolesToAssign));
    }

    @Test
    void assignRolesCandidates() {
        List<RoleIn> currentUserRoles = Stream.of(
                new RoleIn(MEMBER.name(), 10L),
                new RoleIn(WATCHER.name(), 11L)).
                toList();

        List<RoleIn> adminRoles = Collections.singletonList(new RoleIn(ROOT.name(), null));
        UserOut userOut = createTargetAndPrincipalWithRoles(currentUserRoles, adminRoles);

        Page<RoleIn> result = userRoleService.assignRolesCandidates(userOut.getId(), Pageable.ofSize(10));
        assertNotNull(result);
        assertEquals(6, result.getTotalElements());

        assertTrue(result.stream().anyMatch(r -> r.getName().equals(ROOT.name())));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(ADMIN.name())));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 11L));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getResourceId() == 10L));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 11L));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 10L));
    }

    @Test
    void unassignRolesCandidates() {
        List<RoleIn> currentUserRoles = Stream.of(
                        new RoleIn(ROOT.name(), null),
                        new RoleIn(MEMBER.name(), 10L),
                        new RoleIn(WATCHER.name(), 11L)).
                toList();

        List<RoleIn> adminRoles = Collections.singletonList(new RoleIn(ROOT.name(), null));
        UserOut userOut = createTargetAndPrincipalWithRoles(currentUserRoles, adminRoles);

        Page<RoleIn> result = userRoleService.unassignRolesCandidates(userOut.getId(), Pageable.ofSize(10));
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        assertTrue(result.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getResourceId() == 10L));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getResourceId() == 11L));
    }

    @Test
    void assignRolesCandidates_invalidUser() {
        Pageable pageable = Pageable.ofSize(1);
        assertThrows(ResourceNotFoundException.class, () -> userRoleService.assignRolesCandidates(-1, pageable));
    }

    @Test
    void unassignRolesCandidates_invalidUser() {
        Pageable pageable = Pageable.ofSize(1);
        assertThrows(ResourceNotFoundException.class, () -> userRoleService.unassignRolesCandidates(-1, pageable));
    }

    @Test
    void requestRoot() {
        UserOut user = createUserWithRole(USER);
        userRoleService.requestRoot();
        assertEquals(1, userRepo.count());

        UserEntity userEntity = userRepo.findById(user.getId()).orElseThrow();
        assertEquals(Set.of(ROOT, ADMIN, USER), userEntity.getRoles().stream().
                map(r -> RoleName.valueOf(r.getName())).collect(Collectors.toSet()));
    }

    @Test
    void requestRoot_multiple_users() {
        createTargetAndPrincipalWithRoles(USER, USER);
        assertThrows(RootRequestDeclinedException.class, () -> userRoleService.requestRoot());

    }

    @Test
    void requestRoot_multiple_roles() {
        createTargetAndPrincipalWithRoles(
                List.of(new RoleIn(USER.name(), null), new RoleIn(ADMIN.name(), null)),
                Collections.emptyList());
        assertThrows(RootRequestDeclinedException.class, () -> userRoleService.requestRoot());
    }
}