package com.example.apigateway.filter;

import com.example.apigateway.config.GatewayAuthProperties;
import com.example.apigateway.config.RoutePermissions;
import com.example.apigateway.service.JwtService;
import com.example.apigateway.service.TokenBlocklistService;
import com.example.apigateway.utils.PathPermissionTrie;
import com.example.apigateway.utils.PathTrie;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global gateway filter that authenticates requests using JWT access
 * tokens, enforces per-path and per-method permission levels, rejects
 * revoked sessions, and forwards user details to downstream services as
 * headers.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final JwtService jwtService;
    private final GatewayAuthProperties gatewayAuthProperties;
    private final TokenBlocklistService tokenBlocklistService;
    private PathPermissionTrie pathPermissionTrie;
    private PathTrie pathTrie;
    private PathTrie signOutTrie;

    /**
     * Creates the filter with the services and configuration it depends on.
     *
     * @param jwtService            service used to parse and validate access tokens
     * @param gatewayAuthProperties configuration holding public, sign-out and
     *                              route permission rules
     * @param tokenBlocklistService service used to check and store revoked sessions
     */
    public JwtAuthFilter(JwtService jwtService, GatewayAuthProperties gatewayAuthProperties, TokenBlocklistService tokenBlocklistService){
        this.jwtService = jwtService;
        this.gatewayAuthProperties = gatewayAuthProperties;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    /**
     * Builds the permission lookup after construction. Registers both the
     * configured route permissions and the sign-out paths, since sign-out paths
     * also specify which permission levels may use them.
     */
    @PostConstruct
    private void buildPermissionLookup(){
        pathPermissionTrie = new PathPermissionTrie();

        List<RoutePermissions> rules = gatewayAuthProperties.getRoutePermissions();
        if ( rules != null){
            for ( RoutePermissions rule : rules){
                pathPermissionTrie.insert(rule.getPath(), rule.getMethods(), rule.getAllowedLevels());
            }
        }

        // Sign-out paths also carry allowed-levels, so enforce them
        List<RoutePermissions> signOutRules = gatewayAuthProperties.getSignOutPaths();
        if ( signOutRules != null){
            for ( RoutePermissions rule : signOutRules){
                pathPermissionTrie.insert(rule.getPath(), rule.getMethods(), rule.getAllowedLevels());
            }
        }
    }

    /**
     * Builds the lookups for public paths and sign-out paths after construction.
     */
    @PostConstruct
    private void buildPublicPaths(){
        pathTrie = new PathTrie();
        List<RoutePermissions> paths = gatewayAuthProperties.getPublicPaths();
        if ( paths != null){
            for ( RoutePermissions rule : paths){
                pathTrie.insert(rule.getPath(), rule.getMethods());
            }
        }

        signOutTrie = new PathTrie();
        List<RoutePermissions> signOutPaths = gatewayAuthProperties.getSignOutPaths();
        if ( signOutPaths != null){
            for ( RoutePermissions rule : signOutPaths){
                signOutTrie.insert(rule.getPath(), rule.getMethods());
            }
        }
    }

    /**
     * Authenticates and authorizes each incoming request.
     * <p>
     * Public path and method combinations are forwarded without checks. For all
     * others the request must carry a valid Bearer token, the user's permission
     * level must be allowed for the path and method, and the session must not
     * have been revoked. If the request targets a sign-out path, the session is
     * blocklisted. Accepted requests are forwarded with the {@code X-User-Id},
     * {@code X-Permission-Level} and {@code X-Session-Id} headers added.
     *
     * @param exchange the current server exchange
     * @param chain    the gateway filter chain
     * @return a Mono that completes when the request has been rejected or forwarded
     */
    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // Forward request if requested path + method is public
        boolean pathIsPublic = pathTrie.get(path, method);
        if (pathIsPublic) return chain.filter(exchange);

        // Get auth headers for when path is not public, and reject request of not present
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            return rejectUnauthorizedRequest(exchange);
        }

        // Parse claims and reject request if invalid/expired
        Claims claims;
        try{
            claims = jwtService.parseAccessToken(authHeader.substring(7));
        } catch (JwtException e){
            return rejectUnauthorizedRequest(exchange);
        }

        // Get necessary id's from claims, used to
        // - check in cache if session has been signed out of
        // - mutate request to include x-user-id, x-session-id, x-permission-level
        String sessionId = claims.get("sessionId").toString();
        String userId = claims.getSubject();
        String permissionLevel = claims.get("permissionLevel").toString();

        // Check if the requested path + method requires specific permissions
        // if true, and user does not have them, return forbidden.
        boolean userHasPermissions = isPermittedForPath(path, method, Integer.parseInt(permissionLevel));
        if (!userHasPermissions){
            return rejectForbiddenRequest(exchange);
        }

        // Check in cache if access-token is revoked (previously signed out)
        Mono<Boolean> isRevoked = tokenBlocklistService.isRevoked(sessionId);

        // If revoked, reject request,
        // Else, if requested path is a sign-out path, cache session-id
        // then mutate request and forward to service
        return isRevoked.flatMap(revoked -> {
            if (revoked) {
                return rejectUnauthorizedRequest(exchange);
            }

            boolean pathIsSignOut = signOutTrie.get(path, method);
            if (pathIsSignOut){
                tokenBlocklistService.blockSession(sessionId, claims.getExpiration()).subscribe();
            }

            ServerHttpRequest mutatedRequest = mutateRequest(exchange, userId, sessionId, permissionLevel);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        });
    }

    /**
     * Checks whether a permission level is allowed for the given path and method.
     * If no rule matches, the request is currently permitted for any
     * authenticated user.
     *
     * @param path            the request path
     * @param method          the HTTP method of the request
     * @param permissionLevel the permission level of the authenticated user
     * @return true if the level is allowed or no rule applies, false otherwise
     */
    private boolean isPermittedForPath(String path, String method, int permissionLevel){
        List<Integer> allowedLevels = pathPermissionTrie.find(path, method);
        if (allowedLevels == null) return true;
        return allowedLevels.contains(permissionLevel);
    }

    /**
     * Creates a copy of the request with the authenticated user's details added
     * as headers for downstream services.
     *
     * @param exchange        the current server exchange
     * @param userId          the user id taken from the token subject
     * @param sessionId       the session id taken from the token claims
     * @param permissionLevel the permission level taken from the token claims
     * @return the mutated request including the {@code X-User-Id},
     *         {@code X-Permission-Level} and {@code X-Session-Id} headers
     */
    private ServerHttpRequest mutateRequest(ServerWebExchange exchange, String userId, String sessionId, String permissionLevel){
        return exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .header("X-Permission-Level", permissionLevel)
                .header("X-Session-Id", sessionId)
                .build();
    }

    /**
     * Completes the response with HTTP 401 Unauthorized.
     *
     * @param exchange the current server exchange
     * @return a Mono that completes once the response has been sent
     */
    private Mono<Void> rejectUnauthorizedRequest(ServerWebExchange exchange){
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    /**
     * Completes the response with HTTP 403 Forbidden.
     *
     * @param exchange the current server exchange
     * @return a Mono that completes once the response has been sent
     */
    private Mono<Void> rejectForbiddenRequest(ServerWebExchange exchange){
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    /**
     * Returns the order of this filter in the global filter chain. A negative
     * value makes it run before the default gateway filters.
     *
     * @return the filter order, {@code -1}
     */
    @Override
    public int getOrder(){
        return -1;
    }
}
