package com.example.imageservice.exception;

import com.example.imageservice.dto.FieldError;
import com.example.imageservice.utils.ClientResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translates exceptions thrown by controllers and services into JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles request bodies that fail Bean Validation.
     *
     * @param ex the validation exception
     * @return a 400 response with the list of field errors under the key {@code err}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<FieldError>>> handleValidation(MethodArgumentNotValidException ex){
        List<FieldError> errs = new ArrayList<>();
        assert ex.getBindingResult() != null;
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errs.add(new FieldError(
                        err.getField(), err.getDefaultMessage()
                )));

        return ResponseEntity.badRequest().body(Map.of("err", errs));
    }

    /**
     * Handles requests for resources that do not exist.
     *
     * @param ex the exception thrown when no resource was found
     * @return a 404 response with a generic not found message under the key {@code err}
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoResourceFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("err", "Requested resource not found"));
    }

    /**
     * Handles request bodies that cannot be read. Distinguishes a field with the wrong data type, a missing required field, and any other malformed JSON.
     *
     * @param ex the exception thrown when the body could not be parsed
     * @return a 400 response describing the problem
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public  ResponseEntity<Map<String, String>> handleBadJson(HttpMessageNotReadableException ex){
        Map<String, String> err = new HashMap<>();
        if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()){
            String field = ife.getPath().getFirst().getPropertyName();
            String dataType = ife.getTargetType().getSimpleName();
            err.put("err", "Field '" + field + "' must be valid " + dataType);

        } else if (ex.getCause() instanceof MismatchedInputException mie && !mie.getPath().isEmpty()) {
            String field = mie.getPath().getFirst().getPropertyName();
            err.put(field, "Field '" + field + "' is required.");
        } else{
            err.put("err", "Malformed json request body");
        }
        return ResponseEntity.badRequest().body(err);
    }

    /**
     * Handles {@link ConflictException}.
     *
     * @param e the thrown exception
     * @return a 409 response with the exception message
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflictException(ConflictException e){
        return ClientResponse.conflict(e.getMessage());
    }

    /**
     * Handles {@link InternalServerException}.
     *
     * @param e the thrown exception
     * @return a 500 response with the exception message
     */
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Map<String, String>> handleInternalServerException(InternalServerException e){
        return ClientResponse.internalServerErr(e.getMessage());
    }

    /**
     * Handles {@link NotFoundException}.
     *
     * @param e the thrown exception
     * @return a 404 response with the exception message
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException e){
        return ClientResponse.notFound(e.getMessage());
    }

    /**
     * Handles {@link UnauthorizedException}.
     *
     * @param e the thrown exception
     * @return a 403 response with the exception message
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException e){
        return ClientResponse.unAuthorized(e.getMessage());
    }

    /**
     * Handles {@link UnauthenticatedException}.
     *
     * @param e the thrown exception
     * @return a 401 response with the exception message
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthenticatedException(UnauthenticatedException e){
        return ClientResponse.unAuthenticated(e.getMessage());
    }


    /**
     * Handles {@link UnsupportedMediaException}.
     *
     * @param e the thrown exception
     * @return a 415 response with the exception message
     */
    @ExceptionHandler(UnsupportedMediaException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedMediaException(UnsupportedMediaException e){
        return ClientResponse.unsupportedMedia(e.getMessage());
    }
}
