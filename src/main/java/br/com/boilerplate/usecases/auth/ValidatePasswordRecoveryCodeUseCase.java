package br.com.boilerplate.usecases.auth;

import br.com.boilerplate.config.annotations.UseCase;
import br.com.boilerplate.dtos.auth.input.ValidatePasswordRecoveryCodeInputDTO;
import br.com.boilerplate.entities.User;
import br.com.boilerplate.errors.ExceptionCode;
import br.com.boilerplate.errors.exceptions.BusinessRuleException;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.repositories.user.UserRepositoryImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ValidatePasswordRecoveryCodeUseCase {
    private final UserRepositoryImpl repository;

    @Transactional
    public void execute(ValidatePasswordRecoveryCodeInputDTO input){
        var user = repository.findByEmail(input.getEmail()).orElseThrow(() -> new EntityNotFoundException(User.class));

        if (!input.getCode().equals(user.getPasswordRecoveryCode()))
            throw new BusinessRuleException(ExceptionCode.INVALID_PASSWORD_RECOVERY_CODE);
    }
}
