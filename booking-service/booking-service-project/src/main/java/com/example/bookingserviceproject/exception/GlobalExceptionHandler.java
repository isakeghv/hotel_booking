package com.example.bookingserviceproject.exception;

import com.example.bookingserviceproject.dto.FieldError;
import com.example.bookingserviceproject.utils.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.ErrorResponse;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
     * Handles path variables, query parameters and headers that fail Bean
     * Validation, e.g. {@code @Min(1)} on a page number.
     *
     * @param ex the validation exception
     * @return a 400 response with the list of errors under the key {@code err}, the same shape as an invalid body
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, List<FieldError>>> handleParameterValidation(HandlerMethodValidationException ex){
        List<FieldError> errs = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> result.getResolvableErrors()
                .forEach(err -> errs.add(new FieldError(
                        result.getMethodParameter().getParameterName(), err.getDefaultMessage()
                ))));

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
     * Handles {@link ServiceUnavailableException}.
     *
     * @param e the thrown exception
     * @return a 503 response with the exception message
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleServiceUnavailableException(ServiceUnavailableException e){
        return ClientResponse.serviceUnavailable(e.getMessage());
    }

    /**
     * Handles {@link PassThroughException}: another service's refusal, passed on unchanged.
     *
     * @param e the thrown exception
     * @return a response with the other service's status and message
     */
    @ExceptionHandler(PassThroughException.class)
    public ResponseEntity<Map<String, String>> handlePassThroughException(PassThroughException e){
        return ClientResponse.status(e.getStatus(), e.getMessage());
    }

    /**
     * Handles requests without a required header. A missing {@code X-User-Id}
     * means the request did not come through the gateway as a signed-in user.
     *
     * @param ex the exception thrown when the header is missing
     * @return a 401 response if {@code X-User-Id} is missing, otherwise a 400 response
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingHeader(MissingRequestHeaderException ex){
        if ("X-User-Id".equalsIgnoreCase(ex.getHeaderName())) return ClientResponse.unAuthenticated("You must be signed in");
        return ClientResponse.badRequest("Header '" + ex.getHeaderName() + "' is required");
    }

    /**
     * Handles requests without a required query parameter, e.g. the staff list's {@code date}.
     *
     * @param ex the exception thrown when the parameter is missing
     * @return a 400 response naming the missing parameter
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParameter(MissingServletRequestParameterException ex){
        return ClientResponse.badRequest("'" + ex.getParameterName() + "' is required");
    }

    /**
     * Handles a path variable, query parameter or header that cannot be
     * converted to its type, e.g. an invalid UUID.
     *
     * @param ex the exception thrown when the value could not be converted
     * @return a 400 response naming the invalid value
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex){
        return ClientResponse.badRequest("'" + ex.getName() + "' has an invalid value");
    }

    /**
     * Handles everything no other handler covers, without leaking details such
     * as the stack trace. Spring's own errors (e.g. 405 method not allowed,
     * 415 unsupported media type) keep their status.
     *
     * @param ex the exception
     * @return a response with Spring's status for its own errors, otherwise a 500 response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex){
        if (ex instanceof ErrorResponse errorResponse){
            String detail = errorResponse.getBody().getDetail();
            return ClientResponse.status(errorResponse.getStatusCode(), detail != null ? detail : "The request could not be completed");
        }
        log.error("Unexpected error", ex);
        return ClientResponse.internalServerErr("Something went wrong. Please try again");
    }
}
