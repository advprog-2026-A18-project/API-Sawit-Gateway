package my_sawit.api_gateway.filter;

import io.jsonwebtoken.Claims;
import my_sawit.api_gateway.util.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final JwtUtils jwtUtils;

    public AuthenticationFilter(RouteValidator validator, JwtUtils jwtUtils) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (HttpMethod.OPTIONS.equals(request.getMethod())) {
                return chain.filter(exchange);
            }

            if (validator.isSecured.test(request)) {

                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    jwtUtils.validateToken(authHeader);

                    Claims claims = jwtUtils.getClaims(authHeader);
                    String email = claims.getSubject();
                    String role = claims.get("role", String.class);
                    String userId = claims.get("id", String.class);

                    request = exchange.getRequest()
                            .mutate()
                            .headers(headers -> {
                                headers.remove("X-User-Email");
                                headers.remove("X-User-Role");
                                headers.remove("X-User-Id");
                                headers.add("X-User-Email", email);
                                headers.add("X-User-Role", role);
                                headers.add("X-User-Id", userId);
                            })
                            .build();

                } catch (Exception e) {
                    System.out.println("Invalid token: " + e.getMessage());
                    return onError(exchange, "Unauthorized access to application", HttpStatus.UNAUTHORIZED);
                }
            }

            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
    }
}
