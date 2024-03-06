package net.alex.game.queue.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.alex.game.queue.annotation.ValidEmail;
import org.apache.commons.validator.routines.EmailValidator;

public class EmailAddressValidator implements ConstraintValidator<ValidEmail, String> {

    @Override
    public boolean isValid(final String email, final ConstraintValidatorContext context) {
        return EmailValidator.getInstance().isValid(email);
    }
}
