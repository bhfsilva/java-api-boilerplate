package br.com.boilerplate.usecases.user;

import br.com.boilerplate.config.annotations.UseCase;
import br.com.boilerplate.dtos.generics.pagination.output.PaginationOutputDTO;
import br.com.boilerplate.dtos.user.input.FindUsersByFiltersInputDTO;
import br.com.boilerplate.dtos.user.output.UserMinimalOutputDTO;
import br.com.boilerplate.mappers.user.UserMapperImpl;
import br.com.boilerplate.repositories.user.UserRepositoryImpl;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class FindUsersByFiltersUseCase {
    private final UserMapperImpl mapper;
    private final UserRepositoryImpl repository;

    public PaginationOutputDTO<UserMinimalOutputDTO> execute(FindUsersByFiltersInputDTO input) {
        return mapper.toMinimalOutputDTO(repository.findByFilters(input));
    }
}
