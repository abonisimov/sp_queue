package net.alex.game.queue.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import net.alex.game.queue.validator.PasswordConstraintValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface ValidPassword {

    String message() default "Invalid password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
