package net.alex.game.queue.validator;

import net.alex.game.queue.model.in.ResetPasswordIn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordMatchesValidatorTest {

    @Test
    void isValid() {
        PasswordMatchesValidator validator = new PasswordMatchesValidator();
        assertFalse(validator.isValid(ResetPasswordIn.builder().password("x").matchingPassword("y").build(), null));
        assertTrue(validator.isValid(ResetPasswordIn.builder().password("x").matchingPassword("x").build(), null));
    }
}