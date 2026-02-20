package br.com.boilerplate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        var ptBrLocale = Locale.forLanguageTag("pt-BR");
        var localeResolver = new AcceptHeaderLocaleResolver();
        var supportedLocales = List.of(ptBrLocale, Locale.ENGLISH);

        localeResolver.setDefaultLocale(ptBrLocale);
        localeResolver.setSupportedLocales(supportedLocales);

        return localeResolver;
    }
}
