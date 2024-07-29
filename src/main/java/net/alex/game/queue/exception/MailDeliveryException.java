package net.alex.game.queue.exception;

import net.alex.game.queue.annotation.HttpStatusMapping;

import java.io.Serial;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@HttpStatusMapping(status = INTERNAL_SERVER_ERROR)
public class MailDeliveryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3391053034842186229L;

    public MailDeliveryException(Throwable cause) {
        super(cause);
    }
}
