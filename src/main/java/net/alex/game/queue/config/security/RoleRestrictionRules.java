package net.alex.game.queue.config.security;

import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.exception.ResourceNotFoundException;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.model.out.RoleOut;
import net.alex.game.queue.persistence.RoleName;
import net.alex.game.queue.persistence.entity.RoleEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class RoleRestrictionRules {

    public void assertActionsAppliedToMinorPrivilegesOnly(Collection<RoleEntity> targetRoles) {
        TokenAuthentication tokenAuthentication = getAuthentication();
        long targetRank = getRank(targetRoles.stream().map(RoleEntity::getName));
        long principalRank = getRank(tokenAuthentication);

        if (targetRank <= principalRank) {
            throw new AccessRestrictedException();
        }
    }

    public void assertAllowedToAssignRoles(List<RoleIn> targetRoles) {
        TokenAuthentication tokenAuthentication = getAuthentication();
        for (RoleIn roleIn : targetRoles) {
            assertAllowedActionWithRole(toRoleOut(roleIn), toRoleOutList(tokenAuthentication), RoleAction.ASSIGN);
        }
    }

    public void assertAllowedToUnassignRoles(List<RoleIn> targetRoles) {
        TokenAuthentication tokenAuthentication = getAuthentication();
        for (RoleIn roleIn : targetRoles) {
            assertAllowedActionWithRole(toRoleOut(roleIn), toRoleOutList(tokenAuthentication), RoleAction.UNASSIGN);
        }
    }

    public void assertAllowedActionWithRole(RoleOut targetRole,
                                            List<RoleOut> principalRoles,
                                            RoleAction roleAction) {
        checkRankForActionsWithRole(targetRole, principalRoles, roleAction);
        boolean requiresResource = isMaxRoleRequiresResource(principalRoles);
        if (requiresResource) {
            boolean foundAllowingRole = false;
            for (RoleOut principalRole : getRolesWithResource(principalRoles)) {
                if (assertAllowedToAssignResourceRole(targetRole, principalRole)) {
                    foundAllowingRole = true;
                    break;
                }
            }
            if (!foundAllowingRole) {
                throw new AccessRestrictedException();
            }
        }
    }

    private void checkRankForActionsWithRole(RoleOut targetRole,
                                             List<RoleOut> principalRoles,
                                             RoleAction roleAction) {
        long principalRank = getRank(principalRoles);
        if ((roleAction == RoleAction.ASSIGN && targetRole.getRank() < principalRank) ||
                (roleAction == RoleAction.UNASSIGN && targetRole.getRank() <= principalRank)) {
            throw new AccessRestrictedException();
        }
    }

    private TokenAuthentication getAuthentication() {
        return (TokenAuthentication)
                SecurityContextHolder.getContext().getAuthentication();
    }

    private Long getRank(Stream<String> targetRoles) {
        return targetRoles.map(n -> RoleName.valueOf(n).getRank()).
                min(Long::compareTo).orElseThrow(ResourceNotFoundException::new);
    }

    private Long getRank(TokenAuthentication tokenAuthentication) {
        return tokenAuthentication.getAuthorities().stream().
                map(a -> RoleName.valueOf(a.getAuthority().split(":")[0]).getRank()).
                min(Long::compareTo).orElseThrow(ResourceNotFoundException::new);
    }

    private Long getRank(List<RoleOut> roles) {
        return roles.stream().map(RoleOut::getRank).min(Long::compareTo).orElseThrow(ResourceNotFoundException::new);
    }

    private List<RoleOut> toRoleOutList(TokenAuthentication tokenAuthentication) {
        return tokenAuthentication.getAuthorities().stream().map(this::toRoleOut).toList();
    }

    private RoleOut toRoleOut(RoleIn roleIn) {
        return RoleOut.builder().name(roleIn.getName()).resourceId(roleIn.getResourceId()).
                rank(RoleName.valueOf(roleIn.getName()).getRank()).build();
    }

    private RoleOut toRoleOut(GrantedAuthority a) {
        String[] nameResource = a.getAuthority().split(":");
        String name = nameResource[0];
        Long resourceId = null;
        if (nameResource.length > 1 && nameResource[1] != null) {
            resourceId = Long.valueOf(nameResource[1]);
        }
        return RoleOut.builder().name(name).resourceId(resourceId).rank(RoleName.valueOf(name).getRank()).build();
    }

    private boolean isMaxRoleRequiresResource(List<RoleOut> roles) {
        return roles.stream().min(Comparator.comparingLong(RoleOut::getRank)).
                map(e -> RoleName.valueOf(e.getName()).isResourceIdRequired()).
                orElseThrow(ResourceNotFoundException::new);
    }

    private List<RoleOut> getRolesWithResource(List<RoleOut> principalRoles) {
        return principalRoles.stream().filter(r -> RoleName.valueOf(r.getName()).isResourceIdRequired()).toList();
    }

    private boolean assertAllowedToAssignResourceRole(RoleOut targetRole, RoleOut principalRole) {
        return targetRole.getResourceId().equals(principalRole.getResourceId());
    }

    public enum RoleAction {
        ASSIGN,
        UNASSIGN
    }
}
