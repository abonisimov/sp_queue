package net.alex.game.queue.service;

import jakarta.transaction.Transactional;
import net.alex.game.queue.config.security.RoleRestrictionRules;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.persistence.RoleName;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.UserEntity;
import net.alex.game.queue.persistence.repo.RoleRepo;
import net.alex.game.queue.persistence.repo.UserRepo;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
        UserEntity userEntity = userRepo.findById(userId).orElseThrow(ResolutionException::new);
        roleRestrictionRules.assertAllowedToGrantRoles(roles);
        userEntity.setRoles(mergeRoles(roles, userEntity.getRoles()));
        userRepo.save(userEntity);
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

    private List<RoleEntity> getRolesPile(RoleIn role) {
        RoleName roleName = RoleName.valueOf(role.getName());
        return Arrays.stream(RoleName.values()).
                filter(r -> roleName.isResourceIdRequired() == r.isResourceIdRequired()).
                map(e -> new RoleEntity(e.name(), role.getResourceId(), e.getRank())).
                toList();
    }

    private boolean hasRole(RoleEntity claimingRole, Set<RoleEntity> current) {
        for (RoleEntity currentRole : current) {
            if (claimingRole.getName().equals(currentRole.getName()) &&
                    ((claimingRole.getResourceId() == null && currentRole.getResourceId() == null) ||
                    (claimingRole.getResourceId().equals(currentRole.getResourceId())))) {
                return true;
            }
        }
        return false;
    }
}
