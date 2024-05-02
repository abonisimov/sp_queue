package net.alex.game.queue.service;

import net.alex.game.queue.model.out.UserOut;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MailService {

    public void sendRegistrationMail(String email, String token, Locale locale) {
        // todo: implement
    }

    public void sendRestorePasswordMail(UserOut fromUserEntity, String token) {
        // todo: implement
    }
}
