package net.alex.game.queue.exception;

import net.alex.game.queue.annotation.HttpStatusMapping;

import java.io.Serial;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@HttpStatusMapping(status = UNAUTHORIZED)
public class TokenExpiredException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -42015595505498056L;

    public TokenExpiredException() {
        super("Specified token has expired");
    }
}
