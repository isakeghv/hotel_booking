package com.example.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties bound from the {@code gateway} section of
 * {@code application.yml}: public paths, sign-out paths and route
 * permissions.
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayAuthProperties {
    private List<RoutePermissions> publicPaths;
    private List<RoutePermissions> signOutPaths;
    private List<RoutePermissions> routePermissions;

    /**
     * Returns the rules for paths that can be accessed without authentication.
     *
     * @return the list of public path rules, or null if none are configured
     */
    public List<RoutePermissions> getPublicPaths() { return publicPaths; }

    /**
     * Sets the rules for paths that can be accessed without authentication.
     *
     * @param publicPaths the list of public path rules
     */
    public void setPublicPaths(List<RoutePermissions> publicPaths) { this.publicPaths = publicPaths; }

    /**
     * Returns the rules for sign-out paths. A successful request to one of these
     * paths causes the caller's session to be blocklisted.
     *
     * @return the list of sign-out path rules, or null if none are configured
     */
    public List<RoutePermissions> getSignOutPaths() { return signOutPaths; }

    /**
     * Sets the rules for sign-out paths.
     *
     * @param signOutPaths the list of sign-out path rules
     */
    public void setSignOutPaths(List<RoutePermissions> signOutPaths) { this.signOutPaths = signOutPaths; }

    /**
     * Returns the rules that restrict paths to specific permission levels.
     *
     * @return the list of route permission rules, or null if none are configured
     */
    public List<RoutePermissions> getRoutePermissions() { return routePermissions; }

    /**
     * Sets the rules that restrict paths to specific permission levels.
     *
     * @param routePermissions the list of route permission rules
     */
    public void setRoutePermissions(List<RoutePermissions> routePermissions) { this.routePermissions = routePermissions; }
}
