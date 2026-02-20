package br.com.boilerplate.errors.exceptions;

import br.com.boilerplate.errors.ExceptionCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InternalUnexpectedException extends RuntimeException {
    private ExceptionCode code = ExceptionCode.INTERNAL_SERVER_ERROR;
    private String[] templateStringFields = null;

    public InternalUnexpectedException(ExceptionCode code, String... templateStringFields) {
        super(code.toString());
        this.code = code;
        this.templateStringFields = templateStringFields;
    }

    public InternalUnexpectedException(ExceptionCode code) {
        super(code.toString());
        this.code = code;
    }
}
