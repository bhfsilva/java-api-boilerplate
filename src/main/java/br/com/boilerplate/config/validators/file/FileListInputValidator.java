package br.com.boilerplate.config.validators.file;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

public class FileListInputValidator implements ConstraintValidator<File, List<MultipartFile>> {
    private FileInputValidator fileInputValidator;

    @Override
    public void initialize(File fileAnnotation) {
        final var mimeTypes = fileAnnotation.mimeTypes();
        final var extensions = fileAnnotation.extensions();
        final var message = fileAnnotation.message();
        this.fileInputValidator = new FileInputValidator(mimeTypes, extensions, message);
    }

    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
        if (Objects.isNull(multipartFiles))
            return true;

        return multipartFiles.stream().allMatch(file -> fileInputValidator.isValid(file, context));
    }
}
