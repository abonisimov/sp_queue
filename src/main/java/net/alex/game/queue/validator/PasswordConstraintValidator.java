package net.alex.game.queue.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.alex.game.queue.annotation.ValidPassword;
import org.passay.*;

import java.util.Arrays;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(final String password, final ConstraintValidatorContext context) {
        final PasswordValidator validator = new PasswordValidator(Arrays.asList(
                // length between 8 and 30
                new LengthRule(8, 30),
                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),
                // at least one symbol (special character)
                new CharacterRule(EnglishCharacterData.Special, 1),
                // define some illegal sequences that will fail when >= 5 chars long
                // alphabetical is of the form 'abcde', numerical is '34567', qwery is 'asdfg'
                // the false parameter indicates that wrapped sequences are allowed; e.g. 'xyzabc'
                new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false),
                new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false),
                new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false),
                // no whitespace
                new WhitespaceRule()));
        final RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(String.join(" ", validator.getMessages(result))).
                addConstraintViolation();
        return false;
    }

}
