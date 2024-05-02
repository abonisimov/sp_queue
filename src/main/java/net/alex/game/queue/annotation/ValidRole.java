package net.alex.game.queue.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import net.alex.game.queue.validator.RoleValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = RoleValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ValidRole {

    String message() default "Invalid role name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
