package br.com.boilerplate.usecases.auth;

import br.com.boilerplate.config.annotations.UseCase;
import br.com.boilerplate.dtos.auth.input.RequirePasswordRecoveryInputDTO;
import br.com.boilerplate.dtos.email.input.SendEmailInputDTO;
import br.com.boilerplate.entities.User;
import br.com.boilerplate.enums.EmailTemplate;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.repositories.user.UserRepositoryImpl;
import br.com.boilerplate.services.RandomCodeService;
import br.com.boilerplate.services.SmtpEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UseCase
@Slf4j
@RequiredArgsConstructor
public class RequirePasswordRecoveryUseCase {
    private final UserRepositoryImpl repository;
    private final SmtpEmailService smtpEmailService;
    private final RandomCodeService randomCodeService;

    public void execute(RequirePasswordRecoveryInputDTO input){
        var user = repository.findByEmail(input.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(User.class));

        var code = randomCodeService.generate();
        updateUser(user, code);
        sendEmail(code, user.getEmail());
    }

    private void updateUser(User user, String code){
        user.setPasswordRecoveryCode(code);
        repository.save(user);
    }

    private void sendEmail(String newPassword, String receiver){
        Map<String, Object> data = new HashMap<>();
        data.put("code", newPassword);

        var template = smtpEmailService.processTemplate(data, EmailTemplate.FORGOT_PASSWORD);
        log.info("Generating template: {}", template);

        var emailMessage =
                new SendEmailInputDTO(
                        List.of(receiver),
                        "Alteração de senha",
                        template
                );

        smtpEmailService.sendEmail(emailMessage);
    }
}
