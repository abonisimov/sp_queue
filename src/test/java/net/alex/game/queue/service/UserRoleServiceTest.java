package net.alex.game.queue.service;

import net.alex.game.model.Colony;
import net.alex.game.model.Universe;
import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.exception.ResourceNotFoundException;
import net.alex.game.queue.exception.RootRequestDeclinedException;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.RoleName;
import net.alex.game.queue.persistence.RoleResource;
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
        RoleResource roleResource1 = RoleResource.builder().name(Universe.class.getSimpleName()).id("1").build();
        RoleResource roleResource2 = RoleResource.builder().name(Colony.class.getSimpleName()).id("2").build();

        List<RoleIn> currentUserRoles = Stream.of(new RoleIn(MEMBER.name(), roleResource1), new RoleIn(WATCHER.name(), roleResource2)).toList();
        List<RoleIn> adminRoles = Collections.singletonList(new RoleIn(ROOT.name(), null));
        List<RoleIn> rolesToAssign = Stream.of(new RoleIn(ROOT.name(), null), new RoleIn(OWNER.name(), roleResource1)).toList();

        UserOut userOut = createTargetAndPrincipalWithRoles(currentUserRoles, adminRoles);
        Set<RoleEntity> rolesBefore = userRepo.findById(userOut.getId()).orElseThrow().getRoles();
        assertNotNull(rolesBefore);

        userRoleService.assignRoles(userOut.getId(), rolesToAssign);
        Set<RoleEntity> rolesAfter = userRepo.findById(userOut.getId()).orElseThrow().getRoles();
        assertNotNull(rolesAfter);
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(ROOT.name())));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(ADMIN.name())));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource1)));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource1)));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource1)));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource2)));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource2)));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource2)));
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
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id("1").build();
        List<RoleIn> roles = Collections.singletonList(new RoleIn(MEMBER.name(), roleResource));
        assertThrows(ResourceNotFoundException.class, () -> userRoleService.assignRoles(1, roles));
    }

    @Test
    void assignRoles_invalidRoles() {
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id("1").build();
        List<RoleIn> rolesToAssign = Stream.of(new RoleIn(ROOT.name(), null), new RoleIn(OWNER.name(), roleResource)).toList();
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN);
        long userId = userOut.getId();
        assertThrows(AccessRestrictedException.class, () -> userRoleService.assignRoles(userId, rolesToAssign));
    }

    @Test
    void unassignRoles() {
        RoleResource roleResource1 = RoleResource.builder().name(Universe.class.getSimpleName()).id("1").build();
        RoleResource roleResource2 = RoleResource.builder().name(Colony.class.getSimpleName()).id("2").build();

        List<RoleIn> currentUserRoles = Stream.of(
                        new RoleIn(WATCHER.name(), roleResource1),
                        new RoleIn(MEMBER.name(), roleResource1),
                        new RoleIn(OWNER.name(), roleResource1),
                        new RoleIn(WATCHER.name(), roleResource2),
                        new RoleIn(MEMBER.name(), roleResource2),
                        new RoleIn(OWNER.name(), roleResource2)).
                toList();
        List<RoleIn> adminRoles = Collections.singletonList(new RoleIn(ROOT.name(), null));
        List<RoleIn> rolesToUnassign = Collections.singletonList(new RoleIn(MEMBER.name(), roleResource1));

        UserOut userOut = createTargetAndPrincipalWithRoles(currentUserRoles, adminRoles);
        Set<RoleEntity> rolesBefore = userRepo.findById(userOut.getId()).orElseThrow().getRoles();
        assertNotNull(rolesBefore);

        userRoleService.unassignRoles(userOut.getId(), rolesToUnassign);
        Set<RoleEntity> rolesAfter = userRepo.findById(userOut.getId()).orElseThrow().getRoles();
        assertNotNull(rolesAfter);
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(ROOT.name())));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(ADMIN.name())));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource1)));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource1)));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource1)));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource2)));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource2)));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource2)));
        assertTrue(rolesAfter.stream().anyMatch(r -> r.getName().equals(USER.name())));
        assertFalse(rolesAfter.stream().anyMatch(r -> r.getName().equals(SYSTEM.name())));

        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource1)).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource2)).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource2)).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource2)).count());
        assertEquals(1, rolesAfter.stream().filter(r -> r.getName().equals(USER.name())).count());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource1)).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource1)).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource2)).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource2)).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource2)).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource2)).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource2)).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource2)).findAny().orElseThrow().getId());

        assertEquals(
                rolesBefore.stream().filter(r -> r.getName().equals(USER.name())).findAny().orElseThrow().getId(),
                rolesAfter.stream().filter(r -> r.getName().equals(USER.name())).findAny().orElseThrow().getId());
    }

    @Test
    void unassignRoles_invalidUser() {
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id("1").build();
        List<RoleIn> roles = Collections.singletonList(new RoleIn(MEMBER.name(), roleResource));
        assertThrows(ResourceNotFoundException.class, () -> userRoleService.unassignRoles(1, roles));
    }

    @Test
    void unassignRoles_invalidRoles() {
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id("1").build();
        List<RoleIn> rolesToAssign = Stream.of(new RoleIn(ROOT.name(), null), new RoleIn(OWNER.name(), roleResource)).toList();
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN);
        long userId = userOut.getId();
        assertThrows(AccessRestrictedException.class, () -> userRoleService.unassignRoles(userId, rolesToAssign));
    }

    @Test
    void candidateRolesForAssign() {
        RoleResource roleResource1 = RoleResource.builder().name(Universe.class.getSimpleName()).id("10").build();
        RoleResource roleResource2 = RoleResource.builder().name(Colony.class.getSimpleName()).id("11").build();

        List<RoleIn> currentUserRoles = Stream.of(
                new RoleIn(MEMBER.name(), roleResource1),
                new RoleIn(WATCHER.name(), roleResource2)).
                toList();

        List<RoleIn> adminRoles = Collections.singletonList(new RoleIn(ROOT.name(), null));
        UserOut userOut = createTargetAndPrincipalWithRoles(currentUserRoles, adminRoles);

        Page<RoleIn> result = userRoleService.candidateRolesForAssign(userOut.getId(), Pageable.ofSize(10));
        assertNotNull(result);
        assertEquals(6, result.getTotalElements());

        assertTrue(result.stream().anyMatch(r -> r.getName().equals(ROOT.name())));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(ADMIN.name())));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource2)));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(OWNER.name()) && r.getRoleResource().equals(roleResource1)));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource2)));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource1)));
    }

    @Test
    void candidateRolesForUnassign() {
        RoleResource roleResource1 = RoleResource.builder().name(Universe.class.getSimpleName()).id("1").build();
        RoleResource roleResource2 = RoleResource.builder().name(Colony.class.getSimpleName()).id("2").build();

        List<RoleIn> currentUserRoles = Stream.of(
                        new RoleIn(ROOT.name(), null),
                        new RoleIn(MEMBER.name(), roleResource1),
                        new RoleIn(WATCHER.name(), roleResource2)).
                toList();

        List<RoleIn> adminRoles = Collections.singletonList(new RoleIn(ROOT.name(), null));
        UserOut userOut = createTargetAndPrincipalWithRoles(currentUserRoles, adminRoles);

        Page<RoleIn> result = userRoleService.candidateRolesForUnassign(userOut.getId(), Pageable.ofSize(10));
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        assertTrue(result.stream().anyMatch(r -> r.getName().equals(MEMBER.name()) && r.getRoleResource().equals(roleResource1)));
        assertTrue(result.stream().anyMatch(r -> r.getName().equals(WATCHER.name()) && r.getRoleResource().equals(roleResource2)));
    }

    @Test
    void candidateRolesForAssign_invalidUser() {
        Pageable pageable = Pageable.ofSize(1);
        assertThrows(ResourceNotFoundException.class, () -> userRoleService.candidateRolesForAssign(-1, pageable));
    }

    @Test
    void candidateRolesForUnassign_invalidUser() {
        Pageable pageable = Pageable.ofSize(1);
        assertThrows(ResourceNotFoundException.class, () -> userRoleService.candidateRolesForUnassign(-1, pageable));
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