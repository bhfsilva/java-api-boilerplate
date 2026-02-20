package br.com.boilerplate.usecases.user;

import br.com.boilerplate.config.annotations.UseCase;
import br.com.boilerplate.entities.User;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.repositories.user.UserRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.UUID;

@UseCase
@Slf4j
@RequiredArgsConstructor
public class ValidateEmailUseCase {
    private final TemplateEngine templateEngine;
    private final UserRepositoryImpl repository;

    public String execute(UUID id){
        var user = repository.findByIdIncludeInactive(id).orElseThrow(() -> new EntityNotFoundException(User.class));
        log.info("Validating email: {}", user.getEmail());
        user.setEmailValidatedAt(LocalDateTime.now());
        repository.save(user);
        return this.getValidatedEmailPage();
    }

    private String getValidatedEmailPage(){
        var context = new Context();
        return templateEngine.process("email-validation-page", context);
    }
}
