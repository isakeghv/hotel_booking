package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.OccupiedRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link OccupiedRoom} entities.
 */
public interface OccupiedRoomRepository extends JpaRepository<OccupiedRoom, UUID> {

    /**
     * Checks whether a room has a period overlapping the given one. Two periods
     * overlap when an existing period starts before the given one ends, and
     * ends after the given one starts. Periods that only touch, such as one
     * ending at 12:00 and the next starting at 12:00, do not overlap.
     *
     * @param roomId the id of the room
     * @param toTime when the given period ends
     * @param fromTime when the given period starts
     * @return true if an overlapping period exists, false otherwise
     */
    boolean existsByRoomIdAndFromTimeLessThanAndToTimeGreaterThan(UUID roomId, LocalDateTime toTime, LocalDateTime fromTime);

    /**
     * Finds the periods of the given rooms that overlap the given period. Uses
     * the same overlap rule as {@link #existsByRoomIdAndFromTimeLessThanAndToTimeGreaterThan}.
     *
     * @param roomIds  the ids of the rooms
     * @param toTime   when the given period ends
     * @param fromTime when the given period starts
     * @return the overlapping periods, or an empty list if there are none
     */
    List<OccupiedRoom> findAllByRoomIdInAndFromTimeLessThanAndToTimeGreaterThan(Collection<UUID> roomIds, LocalDateTime toTime, LocalDateTime fromTime);

    /**
     * Finds the periods that are either ongoing or in the future, meaning they
     * end after the given time, sorted by when they start. The room and its
     * room type are fetched in the same query.
     *
     * @param booked true for periods caused by bookings, false for periods set by an employee, manager or owner
     * @param now    the current time
     * @return the matching periods, or an empty list if there are none
     */
    @EntityGraph(attributePaths = {"room", "room.roomType"})
    List<OccupiedRoom> findAllByBookedAndToTimeAfterOrderByFromTimeAsc(boolean booked, LocalDateTime now);

    /**
     * Deletes the given periods, but only those caused by bookings, so periods
     * set as unavailable by staff are never deleted by this.
     *
     * @param occupiedIds the ids of the periods, the same as the booking's item ids
     * @return the number of periods deleted
     */
    long deleteAllByOccupiedIdInAndBookedTrue(Collection<UUID> occupiedIds);
}
