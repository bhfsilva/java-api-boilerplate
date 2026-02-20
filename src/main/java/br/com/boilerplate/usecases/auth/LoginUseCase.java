package br.com.boilerplate.usecases.auth;

import br.com.boilerplate.config.annotations.UseCase;
import br.com.boilerplate.dtos.auth.input.LoginInputDTO;
import br.com.boilerplate.dtos.auth.output.LoginOutputDTO;
import br.com.boilerplate.errors.ExceptionCode;
import br.com.boilerplate.errors.exceptions.UnauthorizedException;
import br.com.boilerplate.security.dto.UserDetailsDTO;
import br.com.boilerplate.security.services.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@UseCase
@RequiredArgsConstructor
public class LoginUseCase {
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public LoginOutputDTO execute(LoginInputDTO input){
        var usernamePassword = new UsernamePasswordAuthenticationToken(input.getEmail().trim(), input.getPassword());
        var auth = authenticationManager.authenticate(usernamePassword);
        var userDetails = (UserDetailsDTO) auth.getPrincipal();

        if (!userDetails.getUser().isActive())
            throw new UnauthorizedException(ExceptionCode.BAD_CREDENTIALS);

        var token = jwtTokenService.generateToken(userDetails);
        return new LoginOutputDTO(token);
    }
}
