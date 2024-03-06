package net.alex.game.queue.exception;

import net.alex.game.queue.annotation.HttpStatusMapping;

import java.io.Serial;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@HttpStatusMapping(status = NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6888910583819500339L;

    public ResourceNotFoundException() {
        super("Specified resource is not found");
    }
}
