package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.dto.inventory.InventoryPrimaryImage;
import com.example.bookingserviceproject.dto.response.MyBookingsResponse;
import com.example.bookingserviceproject.model.Booking;
import org.springframework.stereotype.Component;

/**
 * Converts bookings into the overviews of {@link MyBookingsResponse}.
 */
@Component
public class MyBookingsResMapper {
    private static final MyBookingsResponse.Image NO_IMAGE = new MyBookingsResponse.Image(null, null);

    /**
     * Builds the overview of one booking.
     *
     * @param booking the booking
     * @param rooms   the number of rooms in the booking
     * @param image   the primary image of its first room type, or null if there is none or it could not be fetched
     * @return the overview, with an image whose fields are null if there is no image
     */
    public MyBookingsResponse.Overview toOverview(Booking booking, int rooms, InventoryPrimaryImage image){
        return new MyBookingsResponse.Overview(
                booking.getBookingId(),
                booking.getStartTime(),
                booking.getEndTime(),
                rooms,
                booking.getTotalPrice(),
                booking.getCurrency(),
                image == null ? NO_IMAGE : new MyBookingsResponse.Image(image.filePath(), image.altText()));
    }
}
