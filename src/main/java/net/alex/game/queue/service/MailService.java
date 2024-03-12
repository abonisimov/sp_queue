package net.alex.game.queue.service;

import net.alex.game.queue.model.out.UserOut;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    public void sendRegistrationMail(String token) {
        // todo: implement
    }

    public void sendRestorePasswordMail(UserOut fromUserEntity, String token) {
        // todo: implement
    }
}
