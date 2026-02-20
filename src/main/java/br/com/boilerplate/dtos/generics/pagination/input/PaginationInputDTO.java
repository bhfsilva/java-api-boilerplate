package br.com.boilerplate.dtos.generics.pagination.input;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationInputDTO {

    @Getter
    @Positive
    private Integer limit = 3;

    private Integer page = 0;

    public Integer getPage() {
        this.page = (this.page - 1);
        return this.page < 0 ? 0 : this.page;
    }
}