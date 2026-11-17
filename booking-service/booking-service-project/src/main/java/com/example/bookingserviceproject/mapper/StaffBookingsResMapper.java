package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.dto.response.StaffBookingOverview;
import com.example.bookingserviceproject.dto.user.BasicUserInfo;
import com.example.bookingserviceproject.model.Booking;
import org.springframework.stereotype.Component;

/**
 * Converts bookings into the overviews of the staff's booking lists.
 */
@Component
public class StaffBookingsResMapper {

    /**
     * Builds the overview of one booking. The user's id always comes from the
     * booking, so staff can find the user even without the user-service's answer.
     *
     * @param booking the booking
     * @param rooms   the number of rooms to show
     * @param user    the user's basic information, or null if the user-service could not be asked
     * @return the overview, with the names and {@code isActive} null if there is no user information
     */
    public StaffBookingOverview toOverview(Booking booking, int rooms, BasicUserInfo user){
        StaffBookingOverview.User guest = user == null
                ? new StaffBookingOverview.User(booking.getUserId(), null, null, null)
                : new StaffBookingOverview.User(booking.getUserId(), user.firstName(), user.lastName(), user.isActive());

        return new StaffBookingOverview(
                booking.getBookingId(),
                guest,
                rooms,
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getNights());
    }
}
