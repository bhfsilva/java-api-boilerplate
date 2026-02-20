package br.com.boilerplate.mappers.user;

import br.com.boilerplate.dtos.user.input.CreateUserInputDTO;
import br.com.boilerplate.dtos.user.output.UserDetailedOutputDTO;
import br.com.boilerplate.dtos.user.output.UserMinimalOutputDTO;
import br.com.boilerplate.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserStructMapper {
    UserDetailedOutputDTO toDetailedOutputDTO(User entity);
    UserMinimalOutputDTO toMinimalOutputDTO(User entity);
    User toEntity(CreateUserInputDTO dto);
}
