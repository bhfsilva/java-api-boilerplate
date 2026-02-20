package br.com.boilerplate.config;

import br.com.boilerplate.rest.specs.commons.ApiResponseBusinessRuleException;
import br.com.boilerplate.security.dto.RouteDTO;
import br.com.boilerplate.services.MessageService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    private final MessageService messageService = new MessageService("classpath:i18n/exception_codes");

    @Value("${spring.mvc.servlet.path}")
    private String serverBasePath;

    @Value("${springdoc.swagger-ui.title}")
    private String title;

    @Value("${springdoc.swagger-ui.version}")
    private String version;

    private static final String SWAGGER_PATH_PARAM = "\\{[^}]*}";

    @Bean
    public OpenAPI swaggerInfos() {
        var apiInfo = new Info().title(title).version(version);
        var apiBaseServer = new Server().description("Default Server URL").url(serverBasePath);

        return new OpenAPI()
                .info(apiInfo)
                .addServersItem(apiBaseServer);
    }

    private void setSwaggerPathRoles(Operation swaggerOperation, PathItem.HttpMethod method, String path) {
        var roleBasedRoutes = RouteDTO.getAll().stream().filter(route -> Objects.nonNull(route.getRoles())).toList();
        for (RouteDTO route : roleBasedRoutes) {
            var roles = route.getRolesByMethodAndPath(method.name(), path);

            if (Objects.isNull(roles))
                continue;

            roles = Arrays.stream(roles).map(role -> "`" + role + "`").toArray(String[]::new);
            var requiredRolesDescription = "Required roles: " + String.join(", ", roles);
            var originalDescription = (swaggerOperation.getDescription() == null) ? "" : "<br><br>" + swaggerOperation.getDescription();
            swaggerOperation.setDescription("<b>" + requiredRolesDescription + "</b>" + originalDescription);
            break;
        }
    }

    @Bean
    public OpenApiCustomizer customSwaggerPaths() {
        return openApi -> {
            openApi.getPaths().forEach((swaggerPath, swaggerPathInfo) -> {
                var path = swaggerPath.replaceAll(SWAGGER_PATH_PARAM, "*");
                var swaggerOperations = swaggerPathInfo.readOperationsMap();
                var pathSupportedHttpMethods = swaggerOperations.keySet();

                pathSupportedHttpMethods.forEach(method -> {
                    var operation = swaggerOperations.get(method);

                    if (operation.getSummary() != null)
                        operation.setOperationId(operation.getSummary().toLowerCase().replaceAll(" ", "-"));

                    this.setSwaggerPathRoles(operation, method, path);
                });
            });
        };
    }

    private void includeBusinessRuleExceptionDescriptions(Operation operation, HandlerMethod handlerMethod) {
        var businessRuleExceptionAnnotation = handlerMethod.getMethodAnnotation(ApiResponseBusinessRuleException.class);

        if (businessRuleExceptionAnnotation != null && businessRuleExceptionAnnotation.exceptions().length != 0) {
            var responses = operation.getResponses();
            var response = responses.get("422");
            var exceptionCodes = Arrays.stream(businessRuleExceptionAnnotation.exceptions());

            var errorMessages = exceptionCodes
                    .map(code -> messageService.get(code).replace(".", ""))
                    .map(message -> "<li><b>" + message + "<b>;</li>")
                    .collect(Collectors.joining());

            response.setDescription(response.getDescription() + ": <ul>" + errorMessages + "</ul>");
            responses.put("422", response);
        }

    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            this.includeBusinessRuleExceptionDescriptions(operation, handlerMethod);

            operation.addParametersItem(new Parameter()
                    .in("header")
                    .name("Accept-Language")
                    .description("Locale (e.g., en-US, pt-BR)")
                    .schema(new StringSchema()._default("pt-BR"))
                    .required(false));

            return operation;
        };
    }
}
