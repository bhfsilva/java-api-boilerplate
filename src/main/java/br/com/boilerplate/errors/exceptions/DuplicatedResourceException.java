package br.com.boilerplate.errors.exceptions;

import br.com.boilerplate.errors.ExceptionCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Slf4j
public class DuplicatedResourceException extends RuntimeException {
    private final Class<?> entityClass;
    private List<String> duplicatedResources = null;

    public DuplicatedResourceException(Class<?> entityClass) {
        super(ExceptionCode.DUPLICATED_RESOURCE.toString());
        this.entityClass = entityClass;
    }

    public DuplicatedResourceException(Class<?> entityClass, List<String> duplicatedResources) {
        super(ExceptionCode.DUPLICATED_RESOURCE.toString());
        this.entityClass = entityClass;
        this.duplicatedResources = duplicatedResources;
    }
}
