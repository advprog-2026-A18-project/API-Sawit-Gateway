package my_sawit.api_gateway.filter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/auth-service/auth/register",
            "/auth-service/auth/login",
            "/auth-service/auth/google",
            "/auth-service/auth/refresh",
            "/auth-service/auth/logout",
            "/auth-service/auth/tes"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> {
                        String path = request.getURI().getPath();
                        return path.equals(uri) || path.startsWith(uri + "/");
                    });
}
