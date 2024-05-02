package net.alex.game.queue.service;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.exception.ResourceNotFoundException;
import net.alex.game.queue.model.out.RoleOut;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.repo.RoleRepo;
import net.alex.game.queue.persistence.repo.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoleService {

    private final RoleRepo roleRepo;
    private final UserRepo userRepo;

    public RoleService(RoleRepo roleRepo,
                       UserRepo userRepo) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
    }

    public Page<RoleOut> getRoles(Pageable pageable) {
        return roleRepo.findAll(pageable).map(RoleOut::fromEntity);
    }

    public Page<UserOut> getUsers(long roleId, Pageable pageable) {
        RoleEntity roleEntity = roleRepo.findById(roleId).orElseThrow(ResourceNotFoundException::new);
        return userRepo.findAllByRolesId(roleEntity.getId(), pageable).map(UserOut::fromUserEntity);
    }
}
