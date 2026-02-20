package br.com.boilerplate.errors;

import br.com.boilerplate.errors.exceptions.BusinessRuleException;
import br.com.boilerplate.errors.exceptions.DuplicatedResourceException;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.errors.exceptions.ForbiddenException;
import br.com.boilerplate.errors.exceptions.InternalUnexpectedException;
import br.com.boilerplate.errors.exceptions.UnauthorizedException;
import br.com.boilerplate.services.MessageService;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private final MessageService messageService = new MessageService(
            "classpath:i18n/localizable_classes_names", "classpath:i18n/exception_codes"
    );

    private String formatClassName(Class<?> clazz) {
        if (clazz == null)
            return "";

        var className = clazz.getSimpleName();

        try {
            className = messageService.get(className);
        } catch (NoSuchMessageException e) {
            className = clazz.getSimpleName();
        }

        return className.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    private ErrorResponse buildExceptionResponse(
            Class<?> exceptionClass,
            HttpStatus httpStatus,
            ExceptionCode exceptionCode,
            Object details,
            String... templateFields
    ) {
        var prefixByExceptionName = exceptionClass.getSimpleName().chars()
                .filter(Character::isUpperCase)
                .mapToObj(character -> String.valueOf((char) character))
                .reduce("", String::concat);

        prefixByExceptionName = prefixByExceptionName.length() > 3
                ? prefixByExceptionName.substring(0, 3)
                : prefixByExceptionName;

        var code = MessageFormat.format("{0}-{1}-{2}",
                prefixByExceptionName,
                httpStatus.value(),
                exceptionCode.getExceptionIndex()
        );

        var message = messageService.get(exceptionCode, templateFields);
        return new ErrorResponse()
                .withCode(code)
                .withReason(message)
                .withDetails(details);
    }

    @Override
    //@ExceptionHandler(value = { MethodArgumentNotValidException.class })
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        var fieldNames = exception.getBindingResult().getFieldErrors().stream().map(FieldError::getField).toList();
        var details = new HashSet<>(fieldNames).stream()
                .map(name -> this.buildMethodArgumentNotValidResponse(exception, name))
                .toList();

        log.error(exception.getMessage(), exception);

        final var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.BAD_REQUEST,
                ExceptionCode.API_FIELDS_INVALID,
                details,
                fieldNames.toArray(String[]::new)
        );
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    private Map<String, Object> buildMethodArgumentNotValidResponse(MethodArgumentNotValidException exception, String fieldName) {
        var errors = exception.getBindingResult().getFieldErrors(fieldName).stream().map(FieldError::getDefaultMessage).toList();
        errors = errors.stream().map(this::overrideConversionDefaultErrorMessage).toList();

        return Map.ofEntries(
                Map.entry("field", fieldName),
                Map.entry("messages", errors)
        );
    }

    private String overrideConversionDefaultErrorMessage(String message) {
        final var CONVERSION_ERROR_MESSAGE_PREFIX = "Failed to convert";
        if (message.startsWith(CONVERSION_ERROR_MESSAGE_PREFIX))
            return messageService.get(ExceptionCode.MALFORMED_FIELD);

        return message;
    }

    @Override
    //@ExceptionHandler(value = { HttpMessageNotReadableException.class })
    public ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.BAD_REQUEST,
                ExceptionCode.MALFORMED_REQUEST,
                null
        );
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> internalServerErrorExceptionHandler(Exception exception, WebRequest request) {
        final var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ExceptionCode.INTERNAL_SERVER_ERROR,
                null
        );
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<Object> genericBadRequestExceptionHandler(Exception exception, WebRequest request) {
        final var body = buildExceptionResponse(exception.getClass(), HttpStatus.BAD_REQUEST, ExceptionCode.API_FIELDS_INVALID, null);
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { DuplicateKeyException.class })
    public ResponseEntity<Object> duplicateKeyExceptionHandler(DuplicateKeyException exception, WebRequest request) {
        final var body = buildExceptionResponse(exception.getClass(), HttpStatus.CONFLICT, ExceptionCode.API_FIELDS_INVALID, null);
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = { IllegalArgumentException.class })
    public ResponseEntity<Object> illegalArgumentExceptionHandler(IllegalArgumentException exception, WebRequest request) {
        var exceptionCode = exception.getCause().getClass().equals(UnrecognizedPropertyException.class)
                ? ExceptionCode.JSON_MAPPING_ERROR
                : ExceptionCode.API_FIELDS_INVALID;

        final var body = buildExceptionResponse(exception.getClass(), HttpStatus.BAD_REQUEST, exceptionCode, null);
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { BadCredentialsException.class })
    public ResponseEntity<Object> badCredentialsExceptionHandler(Exception exception, WebRequest request) {
        final var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.UNAUTHORIZED,
                ExceptionCode.BAD_CREDENTIALS,
                null
        );
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(value = { BusinessRuleException.class })
    public ResponseEntity<Object> businessExceptionHandler(BusinessRuleException exception, WebRequest request) {
        final var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                exception.getCode(),
                null
        );
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(value = { InternalUnexpectedException.class })
    public ResponseEntity<Object> handleInternalException(InternalUnexpectedException exception, WebRequest request) {
        var templateFields = new String[]{};

        if (Objects.nonNull(exception.getTemplateStringFields()))
            templateFields = exception.getTemplateStringFields();

        final var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getCode(),
                null,
                templateFields
        );
        log.error(exception.getMessage(), exception);
        return this.handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = { EntityNotFoundException.class })
    public ResponseEntity<Object> entityNotFoundExceptionHandler(EntityNotFoundException exception, WebRequest request) {
        final var entityClassName = formatClassName(exception.getEntityClass());
        final var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.NOT_FOUND,
                exception.getCode(),
                null,
                entityClassName
        );
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = { DuplicatedResourceException.class })
    public ResponseEntity<Object> duplicateResourceExceptionHandler(DuplicatedResourceException exception, WebRequest request) {
        var duplicatedResourcesNames = new String[]{ formatClassName(exception.getEntityClass()) };
        var resources = exception.getDuplicatedResources();

        if (Objects.nonNull(resources) && !resources.isEmpty())
            duplicatedResourcesNames = resources.toArray(String[]::new);

        final var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.CONFLICT,
                ExceptionCode.DUPLICATED_RESOURCE,
                null,
                duplicatedResourcesNames
        );

        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = { UnauthorizedException.class })
    public ResponseEntity<Object> unauthorizedExceptionHandler(UnauthorizedException exception, WebRequest request) {
        final var body = buildExceptionResponse(
                exception.getClass(),
                HttpStatus.UNAUTHORIZED,
                exception.getCode(),
                null
        );
        final var cause = Objects.isNull(exception.getOriginalException()) ? exception : exception.getOriginalException();
        log.error(exception.getMessage(), cause);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(value = { ForbiddenException.class })
    public ResponseEntity<Object> forbiddenExceptionHandler(ForbiddenException exception, WebRequest request) {
        log.error(exception.getMessage(), exception);
        return handleExceptionInternal(exception, null, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }
}
