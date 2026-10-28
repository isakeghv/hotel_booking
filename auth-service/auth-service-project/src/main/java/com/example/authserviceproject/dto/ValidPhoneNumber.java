package com.example.authserviceproject.dto;

import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation constraint for phone numbers, applicable to fields. The check is performed by {@link PhoneNumberValidator}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface ValidPhoneNumber {
    /**
     * The message used when validation fails.
     *
     * @return the validation failure message, {@code "Invalid phone number"} by default
     */
    String message() default "Invalid phone number";

    /**
     * The validation groups this constraint belongs to.
     *
     * @return the groups, none by default
     */
    Class<?>[] groups() default {};

    /**
     * Payload that clients can attach to the constraint.
     *
     * @return the payload types, none by default
     */
    Class<? extends Payload>[] payload() default {};
}
