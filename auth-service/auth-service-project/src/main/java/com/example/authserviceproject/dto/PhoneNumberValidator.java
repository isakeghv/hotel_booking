package com.example.authserviceproject.dto;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator behind {@link ValidPhoneNumber}. Checks phone numbers using the libphonenumber library.
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    /**
     * Checks whether a value is a valid phone number. No default region is supplied when parsing, so the number must include its country calling code in international format.
     *
     * @param value the phone number to check
     * @param context the validation context
     * @return true if the value parses and is a valid number, false if it is null, unparseable or invalid
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context){
        if ( value == null) return false;

        try{
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber number = util.parse(value, null);
            return util.isValidNumber(number);
        } catch (NumberParseException e){
            return false;
        }
    }
}
