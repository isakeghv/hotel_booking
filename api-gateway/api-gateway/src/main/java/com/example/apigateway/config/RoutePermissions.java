package com.example.apigateway.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A single rule from the gateway configuration, tying a path pattern to
 * HTTP methods and (optionally) the permission levels allowed to use it.
 * Used for public paths, sign-out paths and route permissions alike.
 */
public class RoutePermissions {
    private String path;
    private Set<String> methods = new HashSet<>();
    private List<Integer> allowedLevels = new ArrayList<>();

    /**
     * Returns the path pattern this rule applies to.
     *
     * @return the path pattern, e.g. {@code /inventory/roomtypes/{roomTypeId}}
     */
    public String getPath() { return path; }

    /**
     * Sets the path pattern this rule applies to. Segments wrapped in curly
     * braces are treated as variable segments.
     *
     * @param path the path pattern
     */
    public void setPath(String path) { this.path = path; }

    /**
     * Returns the HTTP methods this rule applies to.
     *
     * @return the set of HTTP methods; empty means the rule applies to all methods
     */
    public Set<String> getMethods() { return methods; }

    /**
     * Sets the HTTP methods this rule applies to.
     *
     * @param methods the set of HTTP methods; empty means all methods
     */
    public void setMethods(Set<String> methods) { this.methods = methods; }

    /**
     * Returns the permission levels allowed to access the path with the
     * configured methods.
     *
     * @return the list of allowed permission levels
     */
    public List<Integer> getAllowedLevels() { return allowedLevels; }

    /**
     * Sets the permission levels allowed to access the path with the
     * configured methods. Bound from {@code allowed-levels} in the YAML.
     *
     * @param allowedLevels the list of allowed permission levels
     */
    public void setAllowedLevels(List<Integer> allowedLevels) { this.allowedLevels = allowedLevels; }
}
