package br.com.boilerplate.services;

import br.com.boilerplate.dtos.email.input.SendEmailInputDTO;
import br.com.boilerplate.enums.EmailTemplate;
import br.com.boilerplate.errors.ExceptionCode;
import br.com.boilerplate.errors.exceptions.InternalUnexpectedException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService {
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.enabled}")
    private boolean enabled;

    @Value("${spring.mail.from}")
    private String from;

    @Value("${spring.application.name}")
    private String appName;

    public void sendEmail(SendEmailInputDTO emailMessage) {
        var subject = appName + " - " + emailMessage.getSubject();
        emailMessage.setSubject(subject);

        log.info("the function send email is being executed in the thread: {}", Thread.currentThread().getName());

        if (!enabled){
            log.info("The email is not enabled!");
            return;
        }

        log.info("Sending email...");
        log.info("to: " + emailMessage.getReceivers());
        log.info("subject: " + emailMessage.getSubject());
        log.info("content: " + emailMessage.getContent());

        try {
            var message = emailSender.createMimeMessage();
            var messageHelper = new MimeMessageHelper(message, true, "UTF-8");

            messageHelper.setFrom(this.from);
            messageHelper.setTo(emailMessage.getReceivers().toArray(String[]::new));
            messageHelper.setSubject(emailMessage.getSubject());
            messageHelper.setText(emailMessage.getContent(), true);

            var logoFile = new ClassPathResource("/static/images/logo.png");

            if (logoFile.exists())
                messageHelper.addInline("logoImage", logoFile);

            emailSender.send(message);
            log.info("Email sent");
        } catch (MessagingException e) {
            throw new InternalUnexpectedException(
                    ExceptionCode.EMAIL_NOT_SENT,
                    String.join(", ", emailMessage.getReceivers())
            );
        }
    }

    public String processTemplate(Map<String, Object> data, EmailTemplate template) {
        var context = new Context();
        var variables = new HashMap<>(data);

        context.setVariables(variables);
        return templateEngine.process(template.getPath(), context);
    }
}
