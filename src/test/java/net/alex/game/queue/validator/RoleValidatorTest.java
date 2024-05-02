package net.alex.game.queue.validator;

import net.alex.game.queue.model.in.RoleIn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleValidatorTest {

    @Test
    void isValid() {
        RoleValidator validator = new RoleValidator();
        assertFalse(validator.isValid(new RoleIn("incorrect", null), null));
        assertFalse(validator.isValid(new RoleIn("ROOT", 1L), null));
        assertFalse(validator.isValid(new RoleIn("OWNER", null), null));
        assertFalse(validator.isValid(new RoleIn(null, null), null));
        assertTrue(validator.isValid(new RoleIn("ADMIN", null), null));
        assertTrue(validator.isValid(new RoleIn("OWNER", 1L), null));
    }
}