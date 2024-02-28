package net.alex.game.queue.exception;

import java.io.Serial;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@HttpStatusMapping(status = NOT_FOUND)
public class ThreadNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6888910583819500339L;
}
