package net.alex.game.queue.config.security;

import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.model.out.RoleOut;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.RoleName;
import net.alex.game.queue.persistence.entity.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static net.alex.game.queue.persistence.RoleName.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class RoleRestrictionRulesTest extends AbstractUserTest {

    @Autowired
    private RoleRestrictionRules rules;

    @BeforeEach
    void beforeEach() {
        cleanUserRecords();
    }

    @Test
    void assertActionsAppliedToMinorPrivilegesOnly() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN, ROOT);
        Collection<RoleEntity> roles = userOut.getRoles().stream().map(this::toRoleEntity).toList();
        rules.assertActionsAppliedToMinorPrivilegesOnly(roles);
    }

    @Test
    void assertActionsAppliedToMinorPrivilegesOnly_same_rank() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ADMIN, ADMIN);
        Collection<RoleEntity> roles = userOut.getRoles().stream().map(this::toRoleEntity).toList();
        assertThrows(AccessRestrictedException. class, () -> rules.assertActionsAppliedToMinorPrivilegesOnly(roles));
    }

    @Test
    void assertActionsAppliedToMinorPrivilegesOnly_more_important_rank() {
        UserOut userOut = createTargetAndPrincipalWithRoles(ROOT, ADMIN);
        Collection<RoleEntity> roles = userOut.getRoles().stream().map(this::toRoleEntity).toList();
        assertThrows(AccessRestrictedException. class, () -> rules.assertActionsAppliedToMinorPrivilegesOnly(roles));
    }

    @Test
    void assertAllowedToAssignRoles() {
        assertNoExceptionToAssignRoles(roles(role(ROOT)), roles(role(ADMIN)));
        assertNoExceptionToAssignRoles(roles(role(ADMIN)), roles(role(ADMIN)));
        assertNoExceptionToAssignRoles(roles(role(ADMIN), role(OWNER, 1L)), roles(role(MEMBER, 2L)));
        assertNoExceptionToAssignRoles(roles(role(OWNER, 1L), role(OWNER, 2L)), roles(role(MEMBER, 1L)));
        assertNoExceptionToAssignRoles(roles(role(MEMBER, 1L), role(OWNER, 2L)), roles(role(MEMBER, 1L)));
    }

    @Test
    void assertAllowedToAssignRoles_invalid() {
        assertAccessRestrictedExceptionToAssignRoles(roles(role(ADMIN)), roles(role(ROOT)));
        assertAccessRestrictedExceptionToAssignRoles(roles(role(USER)), roles(role(ROOT)));
        assertAccessRestrictedExceptionToAssignRoles(roles(role(OWNER, 1L)), roles(role(MEMBER, 2L)));
        assertAccessRestrictedExceptionToAssignRoles(roles(role(USER)), roles(role(MEMBER, 2L)));
        assertAccessRestrictedExceptionToAssignRoles(roles(role(MEMBER, 1L)), roles(role(OWNER, 1L)));
        assertAccessRestrictedExceptionToAssignRoles(roles(role(MEMBER, 1L), role(WATCHER, 1L)), roles(role(OWNER, 1L)));
    }

    @Test
    void assertAllowedToUnassignRoles() {
        assertNoExceptionToUnassignRoles(roles(role(ROOT)), roles(role(ADMIN)));
        assertNoExceptionToUnassignRoles(roles(role(ADMIN), role(OWNER, 1L)), roles(role(MEMBER, 2L)));
        assertNoExceptionToUnassignRoles(roles(role(OWNER, 1L), role(OWNER, 2L)), roles(role(MEMBER, 1L)));
        assertNoExceptionToUnassignRoles(roles(role(MEMBER, 1L), role(OWNER, 2L)), roles(role(MEMBER, 1L)));
    }

    @Test
    void assertAllowedToUnassignRoles_invalid() {
        assertAccessRestrictedExceptionToUnassignRoles(roles(role(ADMIN)), roles(role(ROOT)));
        assertAccessRestrictedExceptionToUnassignRoles(roles(role(ADMIN)), roles(role(ADMIN)));
        assertAccessRestrictedExceptionToUnassignRoles(roles(role(USER)), roles(role(ROOT)));
        assertAccessRestrictedExceptionToUnassignRoles(roles(role(OWNER, 1L)), roles(role(MEMBER, 2L)));
        assertAccessRestrictedExceptionToUnassignRoles(roles(role(USER)), roles(role(MEMBER, 2L)));
        assertAccessRestrictedExceptionToUnassignRoles(roles(role(MEMBER, 1L)), roles(role(OWNER, 1L)));
        assertAccessRestrictedExceptionToUnassignRoles(roles(role(MEMBER, 1L), role(WATCHER, 1L)), roles(role(OWNER, 1L)));
    }

    private RoleEntity toRoleEntity(RoleOut roleOut) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(roleOut.getName());
        roleEntity.setRank(roleOut.getRank());
        roleEntity.setResourceId(roleOut.getResourceId());
        return roleEntity;
    }

    private void assertNoExceptionToAssignRoles(List<RoleIn> principalRoles,
                                                List<RoleIn> requestedRoles) {
        cleanUserRecords();
        createTargetAndPrincipalWithRoles(Collections.emptyList(), principalRoles);
        rules.assertAllowedToAssignRoles(requestedRoles);
    }

    private void assertAccessRestrictedExceptionToAssignRoles(List<RoleIn> principalRoles,
                                                              List<RoleIn> requestedRoles) {
        cleanUserRecords();
        createTargetAndPrincipalWithRoles(Collections.emptyList(), principalRoles);
        assertThrows(AccessRestrictedException.class, () -> rules.assertAllowedToAssignRoles(requestedRoles));
    }

    private void assertNoExceptionToUnassignRoles(List<RoleIn> principalRoles,
                                                List<RoleIn> requestedRoles) {
        cleanUserRecords();
        createTargetAndPrincipalWithRoles(Collections.emptyList(), principalRoles);
        rules.assertAllowedToUnassignRoles(requestedRoles);
    }

    private void assertAccessRestrictedExceptionToUnassignRoles(List<RoleIn> principalRoles,
                                                              List<RoleIn> requestedRoles) {
        cleanUserRecords();
        createTargetAndPrincipalWithRoles(Collections.emptyList(), principalRoles);
        assertThrows(AccessRestrictedException.class, () -> rules.assertAllowedToUnassignRoles(requestedRoles));
    }

    private List<RoleIn> roles(RoleIn... roles) {
        return Arrays.stream(roles).toList();
    }

    private RoleIn role(RoleName roleName) {
        return role(roleName, null);
    }

    private RoleIn role(RoleName roleName, Long resourceId) {
        return new RoleIn(roleName.name(), resourceId);
    }
}