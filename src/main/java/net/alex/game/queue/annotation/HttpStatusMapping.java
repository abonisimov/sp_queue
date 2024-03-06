package net.alex.game.queue.annotation;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface HttpStatusMapping {
    HttpStatus status();
}

