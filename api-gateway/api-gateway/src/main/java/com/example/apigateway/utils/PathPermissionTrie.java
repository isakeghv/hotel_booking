package com.example.apigateway.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Trie of path patterns that maps each path and HTTP method to the
 * permission levels allowed to access it. Variable segments such as
 * {@code {id}} match any value.
 */
public class PathPermissionTrie {
    /** The root of the trie, representing the empty path. */
    private final TrieNode node = new TrieNode();

    /**
     * A node in the permission trie. Each node represents one path segment and
     * holds its literal children, an optional variable child that matches any
     * value, and the allowed permission levels per HTTP method for a rule
     * ending at this node. The key {@code *} stands for all methods.
     */
    private static class TrieNode {
        Map<String, TrieNode> children = new HashMap<>();
        TrieNode variableChild;
        Map<String, List<Integer>> levelsByMethod = new HashMap<>();
    }

    /**
     * Inserts a path pattern into the trie and associates the allowed permission
     * levels with each of the given HTTP methods. Segments wrapped in curly
     * braces are stored as a shared variable child.
     *
     * @param path          the path pattern, e.g. {@code /inventory/roomtypes/{roomTypeId}}
     * @param methods       the HTTP methods the levels apply to; null or empty
     *                      registers the levels for all methods
     * @param allowedLevels the permission levels allowed to access the path
     */
    public void insert(String path, Set<String> methods, List<Integer> allowedLevels) {
        String[] segments = path.split("/");
        TrieNode current = node;

        for (String segment : segments) {
            if (segment.isEmpty()) continue;

            if (segment.startsWith("{") && segment.endsWith("}")) {
                if (current.variableChild == null) current.variableChild = new TrieNode();
                current = current.variableChild;
            } else {
                current = current.children.computeIfAbsent(segment, k -> new TrieNode());
            }
        }

        if (methods == null || methods.isEmpty()) {
            current.levelsByMethod.put("*", allowedLevels);
        } else {
            for (String m : methods) current.levelsByMethod.put(m.toUpperCase(), allowedLevels);
        }
    }

    /**
     * Finds the allowed permission levels for a concrete request path and HTTP
     * method. A rule for the exact method takes precedence over a rule that
     * applies to all methods.
     *
     * @param path   the concrete request path, e.g. {@code /inventory/roomtypes/5}
     * @param method the HTTP method of the request, e.g. {@code POST}
     * @return the allowed permission levels, or null if no rule matches the path and method
     */
    public List<Integer> find(String path, String method) {
        String[] segments = path.split("/");
        TrieNode current = node;

        for (String segment : segments) {
            if (segment.isEmpty()) continue;

            TrieNode next = current.children.get(segment);
            if (next == null) next = current.variableChild;
            if (next == null) return null;

            current = next;
        }

        List<Integer> levels = current.levelsByMethod.get(method.toUpperCase());
        return levels != null ? levels : current.levelsByMethod.get("*");
    }
}
