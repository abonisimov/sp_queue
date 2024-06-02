package net.alex.game.queue.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.alex.game.queue.annotation.ValidRole;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.persistence.RoleName;

public class RoleValidator implements ConstraintValidator<ValidRole, RoleIn> {
    @Override
    public boolean isValid(RoleIn roleIn, ConstraintValidatorContext context) {
        try {
            RoleName roleName = RoleName.valueOf(roleIn.getName());
            return (!roleName.isResourceIdRequired() || roleIn.getRoleResource() != null) &&
                    (roleName.isResourceIdRequired() || roleIn.getRoleResource() == null);
        } catch (Exception e) {
            return false;
        }
    }
}
