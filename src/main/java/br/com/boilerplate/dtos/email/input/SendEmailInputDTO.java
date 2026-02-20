package br.com.boilerplate.dtos.email.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class SendEmailInputDTO {

        @NotNull
        List<String> receivers;

        @NotBlank
        String subject;

        @NotBlank
        String content;
}
