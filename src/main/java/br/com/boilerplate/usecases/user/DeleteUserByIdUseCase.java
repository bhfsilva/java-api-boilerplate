package br.com.boilerplate.usecases.user;

import br.com.boilerplate.config.annotations.UseCase;
import br.com.boilerplate.entities.User;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.errors.exceptions.ForbiddenException;
import br.com.boilerplate.repositories.user.UserRepositoryImpl;
import br.com.boilerplate.services.AuthenticationContextService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class DeleteUserByIdUseCase {
    private final UserRepositoryImpl repository;
    private final AuthenticationContextService authContext;

    public void execute(UUID id) {
        var user = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class));
        this.validate(user);
        repository.delete(user);
    }

    private void validate(User user) {
        if (!authContext.isAuthenticatedUser(user) && !authContext.isAuthenticatedUserAdmin())
            throw new ForbiddenException();
    }
}
