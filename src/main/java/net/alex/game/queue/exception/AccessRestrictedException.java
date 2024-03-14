package net.alex.game.queue.exception;

import net.alex.game.queue.annotation.HttpStatusMapping;

import java.io.Serial;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@HttpStatusMapping(status = FORBIDDEN)
public class AccessRestrictedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2007037505488768951L;

    public AccessRestrictedException() {
        super("Access is restricted");
    }
}
