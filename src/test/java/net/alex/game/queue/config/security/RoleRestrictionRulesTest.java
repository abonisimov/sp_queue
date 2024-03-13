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
    void assertAllowedToGrantRoles() {
        assertNoExceptionForGrantRoles(roles(role(ROOT)), roles(role(ADMIN)));
        assertNoExceptionForGrantRoles(roles(role(ADMIN)), roles(role(ADMIN)));
        assertNoExceptionForGrantRoles(roles(role(ADMIN), role(OWNER, 1L)), roles(role(MEMBER, 2L)));
        assertNoExceptionForGrantRoles(roles(role(OWNER, 1L), role(OWNER, 2L)), roles(role(MEMBER, 1L)));
        assertNoExceptionForGrantRoles(roles(role(MEMBER, 1L), role(OWNER, 2L)), roles(role(MEMBER, 1L)));
    }

    @Test
    void assertAllowedToGrantRoles_invalid() {
        assertAccessRestrictedExceptionForGrantRoles(roles(role(ADMIN)), roles(role(ROOT)));
        assertAccessRestrictedExceptionForGrantRoles(roles(role(USER)), roles(role(ROOT)));
        assertAccessRestrictedExceptionForGrantRoles(roles(role(OWNER, 1L)), roles(role(MEMBER, 2L)));
        assertAccessRestrictedExceptionForGrantRoles(roles(role(USER)), roles(role(MEMBER, 2L)));
        assertAccessRestrictedExceptionForGrantRoles(roles(role(MEMBER, 1L)), roles(role(OWNER, 1L)));
        assertAccessRestrictedExceptionForGrantRoles(roles(role(MEMBER, 1L), role(WATCHER, 1L)), roles(role(OWNER, 1L)));
    }

    private RoleEntity toRoleEntity(RoleOut roleOut) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(roleOut.getName());
        roleEntity.setRank(roleOut.getRank());
        roleEntity.setResourceId(roleOut.getResourceId());
        return roleEntity;
    }

    private void assertNoExceptionForGrantRoles(List<RoleIn> principalRoles,
                                                List<RoleIn> requestedRoles) {
        cleanUserRecords();
        createTargetAndPrincipalWithRoles(Collections.emptyList(), principalRoles);
        rules.assertAllowedToGrantRoles(requestedRoles);
    }

    private void assertAccessRestrictedExceptionForGrantRoles(List<RoleIn> principalRoles,
                                                              List<RoleIn> requestedRoles) {
        cleanUserRecords();
        createTargetAndPrincipalWithRoles(Collections.emptyList(), principalRoles);
        assertThrows(AccessRestrictedException.class, () -> rules.assertAllowedToGrantRoles(requestedRoles));
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