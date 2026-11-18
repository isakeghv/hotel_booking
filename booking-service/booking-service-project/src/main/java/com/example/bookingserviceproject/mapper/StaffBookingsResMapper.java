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
    private final UserResMapper userResMapper;

    /**
     * Creates the mapper with the mapper it uses for the guest.
     *
     * @param userResMapper mapper from the user-service's user information to the guest
     */
    public StaffBookingsResMapper(UserResMapper userResMapper){
        this.userResMapper = userResMapper;
    }

    /**
     * Builds the overview of one booking.
     *
     * @param booking the booking
     * @param rooms   the number of rooms to show
     * @param user    the user's basic information, or null if the user-service could not be asked
     * @return the overview, with the names and {@code isActive} null if there is no user information
     */
    public StaffBookingOverview toOverview(Booking booking, int rooms, BasicUserInfo user){
        return new StaffBookingOverview(
                booking.getBookingId(),
                userResMapper.toResponse(booking.getUserId(), user),
                rooms,
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getNights());
    }
}
