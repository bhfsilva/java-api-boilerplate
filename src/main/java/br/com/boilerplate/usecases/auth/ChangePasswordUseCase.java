package br.com.boilerplate.usecases.auth;

import br.com.boilerplate.config.annotations.UseCase;
import br.com.boilerplate.dtos.user.input.ChangePasswordInputDTO;
import br.com.boilerplate.entities.User;
import br.com.boilerplate.errors.ExceptionCode;
import br.com.boilerplate.errors.exceptions.BusinessRuleException;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.repositories.user.UserRepositoryImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@UseCase
@RequiredArgsConstructor
public class ChangePasswordUseCase {
    private final UserRepositoryImpl repository;
    private final BCryptPasswordEncoder bcryptPasswordEncoder;

    @Transactional
    public void execute(ChangePasswordInputDTO input) {
        var user = repository.findByEmail(input.getEmail()).orElseThrow(() -> new EntityNotFoundException(User.class));

        this.validate(user, input);

        user.setPasswordRecoveryCode(null);
        user.setPassword(bcryptPasswordEncoder.encode(input.getPassword()));
        repository.save(user);
    }

    private void validate(User user, ChangePasswordInputDTO input) {
        if (!input.getCode().equals(user.getPasswordRecoveryCode()))
            throw new BusinessRuleException(ExceptionCode.INVALID_PASSWORD_RECOVERY_CODE);
    }
}
