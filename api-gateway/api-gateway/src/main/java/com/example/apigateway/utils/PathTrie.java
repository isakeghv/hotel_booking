package com.example.apigateway.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Trie of path patterns and their HTTP methods, used to check whether a
 * request matches a configured rule. Variable segments such as
 * {@code {id}} match any value.
 */
public class PathTrie {
    /** The root of the trie, representing the empty path. */
    private final TrieNode node = new TrieNode();

    /**
     * A node in the path trie. Each node represents one path segment and holds
     * its literal children, an optional variable child that matches any value,
     * and the HTTP methods registered for a rule ending at this node.
     */
    private static class TrieNode {
        Map<String, TrieNode> children = new HashMap<>();
        TrieNode variableChild;
        Set<String> methods = new HashSet<>();
    }

    /**
     * Inserts a path pattern into the trie together with the HTTP methods it
     * applies to. Segments wrapped in curly braces are stored as a shared
     * variable child, so they match any value at that position.
     *
     * @param path    the path pattern, e.g. {@code /images/fetch/{filename}}
     * @param methods the HTTP methods to register; null or empty registers the
     *                path for all methods
     */
    public void insert(String path, Set<String> methods) {
        String[] segments = path.split("/");
        PathTrie.TrieNode current = node;

        for (String segment : segments) {
            if (segment.isEmpty()) continue;

            if (segment.startsWith("{") && segment.endsWith("}")) {
                if (current.variableChild == null) current.variableChild = new PathTrie.TrieNode();
                current = current.variableChild;
            } else {
                current = current.children.computeIfAbsent(segment, k -> new PathTrie.TrieNode());
            }
        }

        if (methods == null || methods.isEmpty()) {
            current.methods.add("*");
        } else {
            for (String m : methods) current.methods.add(m.toUpperCase());
        }
    }

    /**
     * Checks whether a concrete request path and HTTP method match a registered
     * pattern. Literal segments take precedence over variable segments at each
     * level of the path.
     *
     * @param path   the concrete request path, e.g. {@code /images/fetch/cat.png}
     * @param method the HTTP method of the request, e.g. {@code GET}
     * @return true if a rule ends at the matched node and covers the method,
     *         false otherwise
     */
    public boolean get(String path, String method) {
        String[] segments = path.split("/");
        PathTrie.TrieNode current = node;

        for (String segment : segments) {
            if (segment.isEmpty()) continue;

            PathTrie.TrieNode next = current.children.get(segment);
            if (next == null) next = current.variableChild;
            if (next == null) return false;

            current = next;
        }

        return current.methods.contains("*") || current.methods.contains(method.toUpperCase());
    }
}
