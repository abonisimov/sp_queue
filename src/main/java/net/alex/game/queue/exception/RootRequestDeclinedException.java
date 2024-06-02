package net.alex.game.queue.exception;

import net.alex.game.queue.annotation.HttpStatusMapping;

import java.io.Serial;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@HttpStatusMapping(status = FORBIDDEN)
public class RootRequestDeclinedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2575230308803849022L;

    public RootRequestDeclinedException() {
        super("Root request declined");
    }
}
