package br.com.boilerplate.dtos.generics.pagination.output;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import java.util.List;

@With
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationOutputDTO<T> {

    private List<T> content;

    @Schema(example = "3")
    private Integer page;

    @Schema(example = "1")
    private Integer size;

    @Schema(example = "10")
    private Long totalElements;

    @Schema(example = "3")
    private Integer totalPages;

    @Schema(example = "false")
    private Boolean first;

    private Boolean last;

    public Integer getPage() {
        return (this.page + 1);
    }

    public Integer getTotalPages() {
        return this.totalPages == 0 ? 1 : this.totalPages;
    }
}
