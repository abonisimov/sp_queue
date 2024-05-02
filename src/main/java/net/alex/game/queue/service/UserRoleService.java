package net.alex.game.queue.service;

import jakarta.transaction.Transactional;
import net.alex.game.queue.config.security.RoleRestrictionRules;
import net.alex.game.queue.config.security.TokenAuthentication;
import net.alex.game.queue.exception.AccessRestrictedException;
import net.alex.game.queue.exception.ResourceNotFoundException;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.persistence.RoleName;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import net.alex.game.queue.persistence.repo.RoleRepo;
import net.alex.game.queue.persistence.repo.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

import static net.alex.game.queue.config.security.RoleRestrictionRules.RoleAction.ASSIGN;
import static net.alex.game.queue.config.security.RoleRestrictionRules.RoleAction.UNASSIGN;

@Service
public class UserRoleService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final RoleRestrictionRules roleRestrictionRules;

    public UserRoleService(UserRepo userRepo,
                           RoleRepo roleRepo,
                           RoleRestrictionRules roleRestrictionRules) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.roleRestrictionRules = roleRestrictionRules;
    }

    @Transactional
    public void assignRoles(long userId, List<RoleIn> roles) {
        UserEntity userEntity = userRepo.findById(userId).orElseThrow(ResourceNotFoundException::new);
        roleRestrictionRules.assertAllowedToAssignRoles(roles);
        userEntity.setRoles(mergeRoles(roles, userEntity.getRoles()));
        userRepo.save(userEntity);
    }

    @Transactional
    public void unassignRoles(long userId, List<RoleIn> roles) {
        UserEntity userEntity = userRepo.findById(userId).orElseThrow(ResourceNotFoundException::new);
        roleRestrictionRules.assertAllowedToUnassignRoles(roles);
        Set<RoleEntity> userEntityRoles = userEntity.getRoles();
        userEntityRoles.removeAll(unmergeRoles(roles, userEntityRoles));
        userEntity.setRoles(userEntityRoles);
        userRepo.save(userEntity);
    }

    @Transactional
    public Page<RoleIn> assignRolesCandidates(long userId, Pageable pageable) {
        UserEntity userEntity = userRepo.findById(userId).orElseThrow(ResourceNotFoundException::new);
        List<RoleIn> userRoles = userEntity.getRoles().stream().
                map(r -> new RoleIn(r.getName(), r.getResourceId())).toList();
        TokenAuthentication tokenAuthentication = (TokenAuthentication)
                SecurityContextHolder.getContext().getAuthentication();

        Set<RoleIn> result = new HashSet<>();

        for (RoleName roleName : RoleName.values()) {
            if (roleName.isResourceIdRequired()) {
                for (Long resourceId : roleRepo.getDistinctResourceId()) {
                    if (resourceId != null && checkRole(roleName, resourceId, tokenAuthentication, ASSIGN)) {
                        result.add(RoleIn.builder().name(roleName.name()).resourceId(resourceId).build());
                    }
                }
            } else {
                if (checkRole(roleName, null, tokenAuthentication, ASSIGN)) {
                    result.add(RoleIn.builder().name(roleName.name()).build());
                }
            }
        }

        userRoles.stream().toList().forEach(result::remove);

        return new PageImpl<>(result.stream().toList(), pageable, result.size());
    }

    @Transactional
    public Page<RoleIn> unassignRolesCandidates(long userId, Pageable pageable) {
        UserEntity userEntity = userRepo.findById(userId).orElseThrow(ResourceNotFoundException::new);
        List<RoleIn> userRoles = userEntity.getRoles().stream().
                map(r -> new RoleIn(r.getName(), r.getResourceId())).toList();
        TokenAuthentication tokenAuthentication = (TokenAuthentication)
                SecurityContextHolder.getContext().getAuthentication();

        Set<RoleIn> result = new HashSet<>();
        for (RoleIn userRole : userRoles) {
            if (checkRole(RoleName.valueOf(userRole.getName()), userRole.getResourceId(), tokenAuthentication, UNASSIGN)) {
                result.add(userRole);
            }
        }

        return new PageImpl<>(result.stream().toList(), pageable, result.size());
    }

    private Set<RoleEntity> mergeRoles(List<RoleIn> claiming, Set<RoleEntity> current) {
        for (RoleIn roleIn : claiming) {
            for (RoleEntity roleEntity : getRolesPile(roleIn)) {
                if (!hasRole(roleEntity, current)) {
                    current.add(roleEntity);
                    roleRepo.save(roleEntity);
                }
            }
        }
        return current;
    }

    private Set<RoleEntity> unmergeRoles(List<RoleIn> revoking, Set<RoleEntity> current) {
        Set<RoleEntity> result = new HashSet<>();
        for (RoleIn roleIn : revoking) {
            List<RoleEntity> removeStack = getRolesToUnassign(roleIn, current);
            result.addAll(removeStack);
        }
        return result;
    }

    private List<RoleEntity> getRolesPile(RoleIn role) {
        RoleName roleName = RoleName.valueOf(role.getName());
        return Arrays.stream(RoleName.values()).
                filter(r -> roleName.isResourceIdRequired() == r.isResourceIdRequired() && roleName.getRank() <= r.getRank()).
                map(e -> new RoleEntity(e.name(), role.getResourceId(), e.getRank())).
                toList();
    }

    private boolean hasRole(RoleEntity claimingRole, Set<RoleEntity> current) {
        for (RoleEntity currentRole : current) {
            if (claimingRole.getName().equals(currentRole.getName()) &&
                    ((claimingRole.getResourceId() == null && currentRole.getResourceId() == null) ||
                            Objects.equals(claimingRole.getResourceId(), currentRole.getResourceId()))) {
                return true;
            }
        }
        return false;
    }

    private List<RoleEntity> getRolesToUnassign(RoleIn revoking, Set<RoleEntity> current) {
        return current.stream().filter(rolesToUnnassignFileterPredicate(revoking)).toList();
    }

    private static Predicate<RoleEntity> rolesToUnnassignFileterPredicate(RoleIn revoking) {
        return currentRole -> {
            RoleName roleName = RoleName.valueOf(revoking.getName());
            boolean nonResourceRoleMatch = currentRole.getResourceId() == null &&
                    revoking.getResourceId() == null &&
                    revoking.getName().equals(currentRole.getName());
            boolean resourceRoleMath = currentRole.getResourceId() != null &&
                    currentRole.getResourceId().equals(revoking.getResourceId()) &&
                    (roleName.getRank() >= currentRole.getRank());
            return nonResourceRoleMatch || resourceRoleMath;
        };
    }

    private boolean checkRole(RoleName roleName,
                              Long resourceId,
                              TokenAuthentication tokenAuthentication,
                              RoleRestrictionRules.RoleAction roleAction) {
        try {
            RoleIn roleIn = RoleIn.builder().
                    name(roleName.name()).
                    resourceId(resourceId).
                    build();
            roleRestrictionRules.assertAllowedActionWithRole(roleIn, tokenAuthentication, roleAction);
            return true;
        } catch (AccessRestrictedException ignore) {
            return false;
        }
    }
}
