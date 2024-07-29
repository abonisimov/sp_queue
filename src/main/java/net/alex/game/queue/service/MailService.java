package net.alex.game.queue.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.config.EmailConfig;
import net.alex.game.queue.exception.MailDeliveryException;
import net.alex.game.queue.model.out.UserOut;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class MailService {

    private final EmailConfig emailConfig;
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender emailSender;

    public MailService(EmailConfig emailConfig,
                       SpringTemplateEngine templateEngine,
                       JavaMailSender emailSender) {
        this.emailConfig = emailConfig;
        this.templateEngine = templateEngine;
        this.emailSender = emailSender;
    }

    public void sendRegistrationMail(String email, String token, Locale locale) {
        log.info("Sending registration mail to {}", email);
        Map<String, Object> model = new HashMap<>();
        model.put("token", token);
        sendMessageUsingTemplate("registration.html",
                email,
                emailConfig.getRegistrationSubject(),
                model,
                locale);
    }

    public void sendRestorePasswordMail(UserOut fromUserEntity, String token) {
        log.info("Sending restore password mail to {}", fromUserEntity.getEmail());
        Map<String, Object> model = new HashMap<>();
        model.put("nick", fromUserEntity.getNickName());
        model.put("token", token);
        sendMessageUsingTemplate("restorePassword.html",
                fromUserEntity.getEmail(),
                emailConfig.getRestoreSubject(),
                model,
                fromUserEntity.getLocale());
    }

    private void sendMessageUsingTemplate(String template,
                                          String to,
                                          String subject,
                                          Map<String, Object> templateModel,
                                          Locale locale) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        thymeleafContext.setLocale(locale);
        String htmlBody = templateEngine.process(template, thymeleafContext);
        try {
            sendMessage(to, subject, htmlBody);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new MailDeliveryException(e);
        }
    }

    private void sendMessage(String to,
                             String subject,
                             String text) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(emailConfig.getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        emailSender.send(message);
    }
}
