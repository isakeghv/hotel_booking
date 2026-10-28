package com.example.userserviceproject.dto;

/**
 * Describes a validation failure for a single request field.
 *
 * @param field the name of the field that failed validation
 * @param error the validation message for that field
 */
public record FieldError (String field, String error){}