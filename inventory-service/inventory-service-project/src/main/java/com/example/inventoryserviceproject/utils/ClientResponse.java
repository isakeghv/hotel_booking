package com.example.inventoryserviceproject.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Factory methods for building JSON responses with a consistent shape. Successful responses use a caller-supplied key (or {@code message}), and error responses use the key {@code err}.
 */
public class ClientResponse {


    /**
     * Wraps a single key and value in a map to be used as a response body.
     *
     * @param key the key of the entry
     * @param value the value of the entry
     * @param <T> the type of the value
     * @return an immutable map containing the single entry
     */
    private static <T> Map<String, T> resMap(String key, T value){
        return Map.of(key, value);
    }

    /**
     * Builds a 200 response with a single key and value in the body.
     *
     * @param key the key of the body entry
     * @param value the value of the body entry
     * @param <T> the type of the value
     * @return a 200 response with the entry as body
     */
    public static <T> ResponseEntity<Map<String, T>> ok(String key, T value){
        return ResponseEntity.ok(resMap(key, value));
    }

    /**
     * Builds a 201 response with a single key and value in the body.
     *
     * @param key the key of the body entry
     * @param value the value of the body entry
     * @param <T> the type of the value
     * @return a 201 response with the entry as body
     */
    public static <T> ResponseEntity<Map<String, T>> created(String key, T value){
        return ResponseEntity.status(HttpStatus.CREATED).body(resMap(key, value));
    }

    /**
     * Builds a 200 response with a message in the body under the key {@code message}.
     *
     * @param value the message text
     * @return a 200 response with the message as body
     */
    public static ResponseEntity<Map<String, String>> okMsg(String value){
        return ResponseEntity.ok(resMap("message", value));
    }


    /**
     * Builds a 204 response with a no message in body.
     *
     * @return a 204 response
     */
    public static ResponseEntity<Map<String, String>> okEmpty(){
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(resMap("message", ""));
    }

    /**
     * Builds a 403 response with an error message in the body under the key {@code err}.
     *
     * @param value the error message
     * @return a 403 response with the error as body
     */
    public static ResponseEntity<Map<String, String>> unAuthorized(String value){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resMap("err", value));
    }

    /**
     * Builds a 401 response with an error message in the body under the key {@code err}.
     *
     * @param value the error message
     * @return a 401 response with the error as body
     */
    public static ResponseEntity<Map<String, String>> unAuthenticated(String value){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resMap("err", value));
    }

    /**
     * Builds a 409 response with an error message in the body under the key {@code err}.
     *
     * @param value the error message
     * @return a 409 response with the error as body
     */
    public static ResponseEntity<Map<String, String>> conflict(String value){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resMap("err", value));
    }

    /**
     * Builds a 404 response with an error message in the body under the key {@code err}.
     *
     * @param value the error message
     * @return a 404 response with the error as body
     */
    public static ResponseEntity<Map<String, String>> notFound(String value){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resMap("err", value));
    }

    /**
     * Builds a 500 response with an error message in the body under the key {@code err}.
     *
     * @param value the error message
     * @return a 500 response with the error as body
     */
    public static ResponseEntity<Map<String, String>> internalServerErr(String value){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resMap("err", value));
    }

    /**
     * Builds a 415 response with an error value in the body under the key {@code err}. The value must be a String.
     *
     * @param value the error value
     * @return a 415 response with the error as body
     */
    public static ResponseEntity<Map<String, String>> unsupportedMedia(String value){
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(resMap("err", value));
    }

    /**
     * Builds a 400 response with an error value in the body under the key {@code err}. The value can be any type, such as a list of field errors.
     *
     * @param value the error value
     * @param <T> the type of the value
     * @return a 400 response with the error as body
     */
    public static <T> ResponseEntity<Map<String, T>> badRequest(T value){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resMap("err", value));
    }
}
