package net.alex.game.queue.validator;

import net.alex.game.model.Universe;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.persistence.RoleResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleValidatorTest {

    @Test
    void isValid() {
        RoleResource roleResource = RoleResource.builder().name(Universe.class.getSimpleName()).id("1").build();

        RoleValidator validator = new RoleValidator();
        assertFalse(validator.isValid(new RoleIn("incorrect", null), null));
        assertFalse(validator.isValid(new RoleIn("ROOT", roleResource), null));
        assertFalse(validator.isValid(new RoleIn("OWNER", null), null));
        assertFalse(validator.isValid(new RoleIn(null, null), null));
        assertTrue(validator.isValid(new RoleIn("ADMIN", null), null));
        assertTrue(validator.isValid(new RoleIn("OWNER", roleResource), null));
    }
}