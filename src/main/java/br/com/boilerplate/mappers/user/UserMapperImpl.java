package br.com.boilerplate.mappers.user;

import br.com.boilerplate.config.annotations.Mapper;
import br.com.boilerplate.dtos.generics.pagination.output.PaginationOutputDTO;
import br.com.boilerplate.dtos.user.input.CreateUserInputDTO;
import br.com.boilerplate.dtos.user.output.UserDetailedOutputDTO;
import br.com.boilerplate.dtos.user.output.UserMinimalOutputDTO;
import br.com.boilerplate.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper
@RequiredArgsConstructor
public class UserMapperImpl {
    private final UserStructMapper structMapper;

    public UserDetailedOutputDTO toDetailedOutputDTO(User entity) {
        return structMapper.toDetailedOutputDTO(entity);
    }

    public UserMinimalOutputDTO toMinimalOutputDTO(User entity) {
        return structMapper.toMinimalOutputDTO(entity);
    }

    public List<UserMinimalOutputDTO> toMinimalOutputDTO(List<User> entities) {
        return entities.stream().map(this::toMinimalOutputDTO).toList();
    }

    public PaginationOutputDTO<UserMinimalOutputDTO> toMinimalOutputDTO(Page<User> page) {
        var entities = this.toMinimalOutputDTO(page.getContent());
        return new PaginationOutputDTO<UserMinimalOutputDTO>()
            .withContent(entities)
            .withPage(page.getNumber())
            .withSize(page.getNumberOfElements())
            .withTotalElements(page.getTotalElements())
            .withTotalPages(page.getTotalPages())
            .withFirst(page.isFirst())
            .withLast(page.isLast());
    }

    public User toEntity(CreateUserInputDTO dto) {
        return structMapper.toEntity(dto);
    }
}
