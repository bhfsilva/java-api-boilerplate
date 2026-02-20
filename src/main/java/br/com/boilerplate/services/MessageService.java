package br.com.boilerplate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageService {
    private final ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();

    public MessageService(String... basenames) {
        source.setBasenames(basenames);
    }

    private String toKebabCase(String value) {
        return value
                .replaceAll("_", "-")
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase();
    }

    public String get(Enum<?> key, String... args) {
        return this.get(key.name(), args);
    }

    public String get(String key, String... args) {
        key = this.toKebabCase(key);
        final var exception = new NoSuchMessageException(key);
        try {
            if (source.getBasenameSet().isEmpty())
                throw exception;

            this.source.setDefaultEncoding("UTF-8");
            return this.source.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            log.error(e.toString(), e.getCause());
            throw exception;
        }
    }
}
