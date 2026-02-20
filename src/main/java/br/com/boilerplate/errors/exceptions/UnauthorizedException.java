package br.com.boilerplate.errors.exceptions;

import br.com.boilerplate.errors.ExceptionCode;
import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private ExceptionCode code;
    private final Exception originalException;

    public UnauthorizedException() {
        super(ExceptionCode.UNAUTHORIZED.toString());
        this.code = ExceptionCode.UNAUTHORIZED;
        this.originalException = null;
    }

    public UnauthorizedException(Exception exception) {
        super(ExceptionCode.UNAUTHORIZED.toString());
        this.code = ExceptionCode.UNAUTHORIZED;
        this.originalException = exception;
    }

    public UnauthorizedException(ExceptionCode code) {
        super(code.toString());
        this.code = code;
        this.originalException = null;
    }
}
