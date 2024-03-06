package net.alex.game.queue.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.alex.game.queue.annotation.PasswordMatches;
import net.alex.game.queue.model.PasswordConfirmation;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PasswordConfirmation> {

    @Override
    public boolean isValid(final PasswordConfirmation passwordConfirmation, final ConstraintValidatorContext context) {
        return passwordConfirmation.getPassword().equals(passwordConfirmation.getMatchingPassword());
    }

}
