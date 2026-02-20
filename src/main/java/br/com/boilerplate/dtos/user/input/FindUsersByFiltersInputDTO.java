package br.com.boilerplate.dtos.user.input;

import br.com.boilerplate.dtos.generics.pagination.input.PaginationInputDTO;
import br.com.boilerplate.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindUsersByFiltersInputDTO extends PaginationInputDTO {
    private String name;
    private String email;
    private UserRole role;
    private Boolean active;
}
