package br.com.boilerplate.rest.controllers;

import br.com.boilerplate.dtos.auth.input.LoginInputDTO;
import br.com.boilerplate.dtos.auth.input.RequirePasswordRecoveryInputDTO;
import br.com.boilerplate.dtos.auth.input.ValidatePasswordRecoveryCodeInputDTO;
import br.com.boilerplate.dtos.auth.output.LoginOutputDTO;
import br.com.boilerplate.dtos.user.input.ChangePasswordInputDTO;
import br.com.boilerplate.rest.specs.AuthControllerSpecs;
import br.com.boilerplate.usecases.auth.ChangePasswordUseCase;
import br.com.boilerplate.usecases.auth.LoginUseCase;
import br.com.boilerplate.usecases.auth.RequirePasswordRecoveryUseCase;
import br.com.boilerplate.usecases.auth.ValidatePasswordRecoveryCodeUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController implements AuthControllerSpecs {
    private final LoginUseCase loginUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final RequirePasswordRecoveryUseCase requirePasswordRecoveryUseCase;
    private final ValidatePasswordRecoveryCodeUseCase validatePasswordRecoveryCodeUseCase;

    @PostMapping("/login")
    public LoginOutputDTO login(@RequestBody @Valid LoginInputDTO request) {
        return loginUseCase.execute(request);
    }

    @PatchMapping("/require-password-recovery")
    public void requirePasswordRecovery(@RequestBody @Valid RequirePasswordRecoveryInputDTO request) {
        requirePasswordRecoveryUseCase.execute(request);
    }

    @PatchMapping("/validate-password-recovery-code")
    public void validatePasswordRecoveryCode(@RequestBody @Valid ValidatePasswordRecoveryCodeInputDTO request) {
        validatePasswordRecoveryCodeUseCase.execute(request);
    }

    @PatchMapping("/change-password")
    public void changePassword(@RequestBody @Valid ChangePasswordInputDTO request) {
        changePasswordUseCase.execute(request);
    }
}