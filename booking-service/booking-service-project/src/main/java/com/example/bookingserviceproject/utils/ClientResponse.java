package com.example.bookingserviceproject.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Factory methods for building JSON responses with a consistent shape. Successful responses use the
 * typed bodies in {@link com.example.bookingserviceproject.dto.response.Envelopes}, and error responses
 * use the key {@code err}.
 */
public class ClientResponse {

    private static <T> Map<String, T> resMap(String key, T value){
        return Map.of(key, value);
    }

    /**
     * Builds a 200 response with the given body, one of the records in
     * {@link com.example.bookingserviceproject.dto.response.Envelopes}.
     *
     * @param body the response body
     * @param <T> the type of the body
     * @return a 200 response with the body
     */
    public static <T> ResponseEntity<T> ok(T body){
        return ResponseEntity.ok(body);
    }

    /**
     * Builds a 201 response with the given body, one of the records in
     * {@link com.example.bookingserviceproject.dto.response.Envelopes}.
     *
     * @param body the response body
     * @param <T> the type of the body
     * @return a 201 response with the body
     */
    public static <T> ResponseEntity<T> created(T body){
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * Builds a 204 response without a body.
     *
     * @return a 204 response
     */
    public static ResponseEntity<Void> noContent(){
        return ResponseEntity.noContent().build();
    }

    /**
     * Builds a 503 response with an error message in the body under the key {@code err}.
     *
     * @param value the error message
     * @return a 503 response with the error as body
     */
    public static ResponseEntity<Map<String, String>> serviceUnavailable(String value){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(resMap("err", value));
    }

    /**
     * Builds a response with the given status and an error message in the body under the key {@code err}.
     * Used to pass on another service's refusal unchanged.
     *
     * @param status the status of the response
     * @param value the error message
     * @return a response with the status and the error as body
     */
    public static ResponseEntity<Map<String, String>> status(HttpStatusCode status, String value){
        return ResponseEntity.status(status).body(resMap("err", value));
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
