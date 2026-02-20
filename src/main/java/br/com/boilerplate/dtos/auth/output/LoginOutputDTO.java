package br.com.boilerplate.dtos.auth.output;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginOutputDTO {
    private String token;
}
