package br.com.boilerplate.security.dto;

import br.com.boilerplate.enums.UserRole;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RouteDTO {
    private final Map<HttpMethod, List<String>> pathsByMethod = new HashMap<>();
    private static final List<RouteDTO> existingRoutes = new ArrayList<>();

    @Getter
    private String[] roles;

    public RouteDTO() {
        existingRoutes.add(this);
    }

    public String[] getPathsByMethod(HttpMethod method) {
        if (!this.pathsByMethod.containsKey(method))
            throw new RuntimeException("HTTP " + method.name() + " not supported to this route");

        return this.pathsByMethod.get(method).toArray(String[]::new);
    }

    public String[] getRolesByMethodAndPath(String httpMethod, String path) {
        var method = httpMethod.toUpperCase();
        var paths = this.pathsByMethod.get(HttpMethod.valueOf(method));

        if (Objects.nonNull(paths) && paths.contains(path))
            return this.getRoles();

        return null;
    }

    public RouteDTO setPaths(HttpMethod httpMethod, List<String> paths) {
        this.pathsByMethod.put(httpMethod, paths);
        return this;
    }

    public RouteDTO setRoles(UserRole... roles) {
        this.roles = Arrays.stream(roles).map(UserRole::name).toArray(String[]::new);
        return this;
    }

    public static List<RouteDTO> getAll() {
        return existingRoutes;
    }
}
