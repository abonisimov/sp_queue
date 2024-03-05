package net.alex.game.queue.exception;

import net.alex.game.queue.annotation.HttpStatusMapping;

import java.io.Serial;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@HttpStatusMapping(status = UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8435639098000706276L;

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
