package com.example.userserviceproject.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ClientResponse {

    private static <T> Map<String, T> resMap(String key, T value){
        return Map.of(key, value);
    }

    public static <T> ResponseEntity<Map<String, T>> ok(String key, T value){
        return ResponseEntity.ok(resMap(key, value));
    }

    public static ResponseEntity<Map<String, String>> okMsg(String value){
        return ResponseEntity.ok(resMap("message", value));
    }

    public static ResponseEntity<Map<String, String>> unAuthorized(String value){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resMap("err", value));
    }

    public static ResponseEntity<Map<String, String>> unAuthenticated(String value){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resMap("err", value));
    }

    public static ResponseEntity<Map<String, String>> conflict(String value){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resMap("err", value));
    }

    public static ResponseEntity<Map<String, String>> notFound(String value){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resMap("err", value));
    }

    public static ResponseEntity<Map<String, String>> internalServerErr(String value){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resMap("err", value));
    }

    public static <T> ResponseEntity<Map<String, T>> badRequest(T value){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resMap("err", value));
    }
}
