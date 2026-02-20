package br.com.boilerplate.security.config;

import br.com.boilerplate.security.dto.RouteDTO;
import br.com.boilerplate.security.filters.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static br.com.boilerplate.enums.UserRole.ADMIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationFilter authenticationFilter;

    private static final String[] SWAGGER_RESOURCES = {
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/docs/**"
    };

    private static final RouteDTO PUBLIC_ROUTES = new RouteDTO()
            .setPaths(POST, List.of(
                    "/users",
                    "/login"
            ))
            .setPaths(PATCH, List.of(
                    "/require-password-recovery",
                    "/validate-password-recovery-code",
                    "/change-password"
            ))
            .setPaths(GET, List.of(
                    "/users/*/validate-email"
            ));

    private static final RouteDTO PRIVATE_ROUTES = new RouteDTO()
            .setPaths(GET, List.of(
                    "/users/*"
            ))
            .setPaths(DELETE, List.of(
                    "/users/*"
            ));

    private static final RouteDTO ADMIN_ROUTES = new RouteDTO()
            .setRoles(ADMIN)
            .setPaths(GET, List.of(
                    "/users"
            ));

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(Collections.singletonList("*"));
        cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
        cors.setExposedHeaders(List.of("*"));
        cors.setAllowedHeaders(List.of("*"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    private void setCorsConfiguration(CorsConfigurer<HttpSecurity> cors) {
        cors.configurationSource(corsConfigurationSource());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(this::setCorsConfiguration)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PATCH, PUBLIC_ROUTES.getPathsByMethod(PATCH)).permitAll()
                        .requestMatchers(POST, PUBLIC_ROUTES.getPathsByMethod(POST)).permitAll()
                        .requestMatchers(GET, PUBLIC_ROUTES.getPathsByMethod(GET)).permitAll()

                        .requestMatchers(GET, PRIVATE_ROUTES.getPathsByMethod(GET)).authenticated()
                        .requestMatchers(DELETE, PRIVATE_ROUTES.getPathsByMethod(DELETE)).authenticated()

                        .requestMatchers(GET, ADMIN_ROUTES.getPathsByMethod(GET)).hasAnyAuthority(ADMIN_ROUTES.getRoles())

                        .requestMatchers(SWAGGER_RESOURCES).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
