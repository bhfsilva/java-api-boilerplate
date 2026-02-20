package br.com.boilerplate.config.validators.file;

import br.com.boilerplate.errors.ExceptionCode;
import br.com.boilerplate.services.MessageService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
public class FileInputValidator implements ConstraintValidator<File, MultipartFile> {
    private final MessageService messageService = new MessageService("classpath:i18n/exception_codes");
    private String[] mimeTypes;
    private String[] extensions;
    private String message;

    @Override
    public void initialize(File fileAnnotation) {
        this.mimeTypes = fileAnnotation.mimeTypes();
        this.extensions = fileAnnotation.extensions();
        this.message = fileAnnotation.message();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        if (Objects.isNull(multipartFile))
            return true;

        final var isValidExtension = this.validateExtension(extensions, multipartFile.getOriginalFilename());
        final var isValidMimeType = this.validateMimeType(mimeTypes, multipartFile.getContentType());

        if (isValidExtension && isValidMimeType)
            return true;

        var supportedMimeTypes = Arrays.stream(mimeTypes);
        var supportedExtensions = Arrays.stream(extensions);

        var supportedFiles = Stream.concat(supportedExtensions, supportedMimeTypes)
                .filter(item -> !Objects.equals(item, "*"))
                .toArray(String[]::new);

        var supportedFilesIdentifiers = String.join(", ", supportedFiles);
        this.setCustomMessage(context, supportedFilesIdentifiers);

        return false;
    }

    private Boolean validateExtension(String[] supportedExtensions, String filename) {
        if (Arrays.asList(supportedExtensions).contains("*"))
            return true;

        var fileExtension = StringUtils.getFilenameExtension(filename);

        for (var extension : supportedExtensions) {
            extension = extension.startsWith(".") ? extension.substring(1) : extension;
            extension = "^" + extension.replace("*", ".*") + "$";
            if (Pattern.matches(extension, fileExtension))
                return true;
        }
        return false;
    }

    private Boolean validateMimeType(String[] supportedMimeTypes, String fileMimeType) {
        if (Arrays.asList(supportedMimeTypes).contains("*"))
            return true;

        for (var mimeTypeRegex : supportedMimeTypes) {
            mimeTypeRegex = "^" + mimeTypeRegex.replace("*", ".*") + "$";
            if (Pattern.matches(mimeTypeRegex, fileMimeType))
                return true;
        }
        return false;
    }

    private void setCustomMessage(ConstraintValidatorContext context, String supportedFilesIdentifiers) {
        context.disableDefaultConstraintViolation();
        this.message = messageService.get(ExceptionCode.INVALID_FILE_TYPE, supportedFilesIdentifiers);
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
