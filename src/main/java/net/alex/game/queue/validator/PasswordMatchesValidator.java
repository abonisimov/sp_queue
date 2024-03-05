package net.alex.game.queue.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.alex.game.queue.annotation.PasswordMatches;
import net.alex.game.queue.model.UserIn;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserIn> {

    @Override
    public boolean isValid(final UserIn userIn, final ConstraintValidatorContext context) {
        return userIn.getPassword().equals(userIn.getMatchingPassword());
    }

}
