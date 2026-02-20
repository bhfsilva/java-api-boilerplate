package br.com.boilerplate.errors.exceptions;

import br.com.boilerplate.errors.ExceptionCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityNotFoundException extends RuntimeException {
    private Class<?> entityClass = null;
    private ExceptionCode code = ExceptionCode.ENTITY_NOT_FOUND;

    public EntityNotFoundException(ExceptionCode code) {
        super(code.toString());
        this.code = code;
    }

    public EntityNotFoundException(Class<?> entityClass) {
        super(ExceptionCode.ENTITY_NOT_FOUND.toString());
        this.entityClass = entityClass;
    }
}
