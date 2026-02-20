package br.com.boilerplate;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Map;

@Slf4j
@EnableJpaAuditing
@SpringBootApplication
@SecurityScheme(type = SecuritySchemeType.HTTP, name = "jwt", scheme = "bearer", bearerFormat = "JWT")
public class BoilerplateApplication {

    @Value("${springdoc.swagger-ui.version}")
    private String version;

    @Value("${spring.application.name}")
    private String appName;

    private static Map<String, String> staticValuesMap;

    @PostConstruct
    public void init() {
        staticValuesMap = Map.ofEntries(
            Map.entry("version", version),
            Map.entry("name", appName)
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(BoilerplateApplication.class, args);

        log.info("""
                \s
                 _           _ _                 _       _      \s
                | |__   ___ (_) | ___ _ __ _ __ | | __ _| |_ ___\s
                | '_ \\ / _ \\| | |/ _ \\ '__| '_ \\| |/ _` | __/ _ \\
                | |_) | (_) | | |  __/ |  | |_) | | (_| | ||  __/
                |_.__/ \\___/|_|_|\\___|_|  | .__/|_|\\__,_|\\__\\___|
                                          |_|                   \s
                \s
                {} :: {}
                \s""",
                staticValuesMap.get("name").toUpperCase(),
                staticValuesMap.get("version")
        );
    }
}
