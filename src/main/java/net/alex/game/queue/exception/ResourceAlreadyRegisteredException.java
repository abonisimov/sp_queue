package net.alex.game.queue.exception;

import net.alex.game.queue.annotation.HttpStatusMapping;

import java.io.Serial;

import static org.springframework.http.HttpStatus.CONFLICT;

@HttpStatusMapping(status = CONFLICT)
public class ResourceAlreadyRegisteredException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 718396192092844580L;

    private final String resource;

    public ResourceAlreadyRegisteredException(String resource, String message) {
        super(message);
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
