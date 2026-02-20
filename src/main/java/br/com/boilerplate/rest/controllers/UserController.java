package br.com.boilerplate.rest.controllers;

import br.com.boilerplate.dtos.generics.pagination.output.PaginationOutputDTO;
import br.com.boilerplate.dtos.user.input.CreateUserInputDTO;
import br.com.boilerplate.dtos.user.input.FindUsersByFiltersInputDTO;
import br.com.boilerplate.dtos.user.output.UserDetailedOutputDTO;
import br.com.boilerplate.dtos.user.output.UserMinimalOutputDTO;
import br.com.boilerplate.rest.specs.UserControllerSpecs;
import br.com.boilerplate.usecases.user.CreateUserUseCase;
import br.com.boilerplate.usecases.user.DeleteUserByIdUseCase;
import br.com.boilerplate.usecases.user.FindUserByIdUseCase;
import br.com.boilerplate.usecases.user.FindUsersByFiltersUseCase;
import br.com.boilerplate.usecases.user.ValidateEmailUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController implements UserControllerSpecs {
    private final CreateUserUseCase createUserUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final ValidateEmailUseCase validateEmailUseCase;
    private final DeleteUserByIdUseCase deleteUserByIdUseCase;
    private final FindUsersByFiltersUseCase findUsersByFilterUseCase;

    @PostMapping
    public void create(@RequestBody @Valid CreateUserInputDTO request) {
        createUserUseCase.execute(request);
    }

    @GetMapping
    public PaginationOutputDTO<UserMinimalOutputDTO> list(@ParameterObject @ModelAttribute @Valid FindUsersByFiltersInputDTO request) {
        return findUsersByFilterUseCase.execute(request);
    }

    @GetMapping("/{id}")
    public UserDetailedOutputDTO findById(@PathVariable UUID id, @RequestParam(defaultValue = "true") Boolean includeInactive) {
        return findUserByIdUseCase.execute(id, includeInactive);
    }

    @GetMapping("/{id}/validate-email")
    public String validateEmail(@PathVariable UUID id) {
        return validateEmailUseCase.execute(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        deleteUserByIdUseCase.execute(id);
    }
}
