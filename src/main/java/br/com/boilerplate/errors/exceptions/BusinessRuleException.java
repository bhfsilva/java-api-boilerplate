package br.com.boilerplate.errors.exceptions;

import br.com.boilerplate.errors.ExceptionCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessRuleException extends RuntimeException {
    private final ExceptionCode code;

    public BusinessRuleException(ExceptionCode code) {
        super(code.toString());
        this.code = code;
    }
}

