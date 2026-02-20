package br.com.boilerplate.usecases.user;

import br.com.boilerplate.config.annotations.UseCase;
import br.com.boilerplate.dtos.user.output.UserDetailedOutputDTO;
import br.com.boilerplate.entities.User;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.errors.exceptions.ForbiddenException;
import br.com.boilerplate.mappers.user.UserMapperImpl;
import br.com.boilerplate.repositories.user.UserRepositoryImpl;
import br.com.boilerplate.services.AuthenticationContextService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class FindUserByIdUseCase {
    private final UserMapperImpl mapper;
    private final UserRepositoryImpl repository;
    private final AuthenticationContextService authContext;

    public UserDetailedOutputDTO execute(UUID id, Boolean includeInactive) {
        var user = repository.findByIdIncludeInactive(id).orElseThrow(() -> new EntityNotFoundException(User.class));
        this.validate(user, includeInactive);
        return mapper.toDetailedOutputDTO(user);
    }

    private void validate(User user, Boolean includeInactive) {
        if (!authContext.isAuthenticatedUser(user) && !authContext.isAuthenticatedUserAdmin())
            throw new ForbiddenException();

        if (!includeInactive && !user.isActive())
            throw new EntityNotFoundException(User.class);
    }
}
