package com.example.inventoryserviceproject.mapper;

import com.example.inventoryserviceproject.dto.response.ExtraAvailabilityResponse;
import com.example.inventoryserviceproject.model.Extra;
import com.example.inventoryserviceproject.utils.PriceUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Converts a priced extra into an {@link ExtraAvailabilityResponse}.
 */
@Component
public class ExtraAvailabilityResMapper {

    /**
     * Builds the response for an extra priced for the user's stay.
     *
     * @param extra     the extra
     * @param amount    the number of units
     * @param from      the start of the stay the price is based on
     * @param to        the end of the stay the price is based on
     * @param nights    the number of nights in the stay
     * @param total     the price of the extra for the whole stay
     * @param expiresAt when the user's cart expires
     * @return the response object
     */
    public ExtraAvailabilityResponse toResponse(Extra extra, int amount, LocalDateTime from, LocalDateTime to, int nights, int total, Instant expiresAt){
        return new ExtraAvailabilityResponse(
                PriceUtils.CURRENCY,
                expiresAt,
                new ExtraAvailabilityResponse.ExtraInfo(extra.getExtraId(), extra.getName(), extra.getPrice(), extra.getIsPriceFixed()),
                amount,
                new ExtraAvailabilityResponse.StayInfo(from, to, nights),
                total);
    }
}
