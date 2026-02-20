package br.com.boilerplate.dtos.user.output;

import br.com.boilerplate.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UserDetailedOutputDTO {
    private UUID id;

    private String name;

    @Schema(example = "example@gmail")
    private String email;

    private UserRole role;

    private LocalDateTime createdAt;

    private LocalDateTime emailValidatedAt;
}
