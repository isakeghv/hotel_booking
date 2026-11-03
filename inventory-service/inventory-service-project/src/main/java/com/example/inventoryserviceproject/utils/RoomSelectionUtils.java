package com.example.inventoryserviceproject.utils;

import com.example.inventoryserviceproject.model.Room;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects which free rooms to give a user, keeping their rooms as close to
 * each other as possible. Room numbers are floor + number on the floor, so
 * the floor is the room number without its last two digits (e.g. 201 is on floor 2).
 */
public final class RoomSelectionUtils {

    private RoomSelectionUtils(){}

    /**
     * Picks the given amount of rooms, as close to each other and to the rooms
     * already in the user's cart as possible. In order of preference:
     * <ol>
     *     <li>All on one floor, preferably a floor the cart already has rooms on</li>
     *     <li>The smallest gap between the first and last room number</li>
     *     <li>Closest to the rooms already in the cart</li>
     *     <li>Lowest floor and room number, leaving neighbours free for later</li>
     * </ol>
     * If no floor has enough free rooms, starts on the floor with the most free
     * rooms and fills up from the nearest floors.
     *
     * @param candidates      the free rooms to pick from
     * @param amount          the number of rooms to pick
     * @param cartRoomNumbers the room numbers already in the user's cart
     * @return the picked rooms, or an empty list if there are fewer candidates than the amount
     */
    public static List<Room> pickClosestRooms(List<Room> candidates, int amount, Collection<Integer> cartRoomNumbers){
        if (candidates.size() < amount) return List.of();

        // Floor -> its free rooms sorted by number, floors sorted lowest first
        TreeMap<Integer, List<Room>> roomsByFloor = candidates.stream()
                .sorted(Comparator.comparingInt(Room::getRoomNumber))
                .collect(Collectors.groupingBy(room -> floorOf(room.getRoomNumber()), TreeMap::new, Collectors.toList()));
        Set<Integer> cartFloors = cartRoomNumbers.stream().map(RoomSelectionUtils::floorOf).collect(Collectors.toSet());

        // Slide a window of `amount` neighbouring rooms along each floor, and keep the best one.
        // Floors and windows are checked lowest first, and only a strictly better score replaces the best,
        // so ties go to the lowest floor and room number
        List<Room> best = null;
        long[] bestScore = null;
        for (Map.Entry<Integer, List<Room>> floor : roomsByFloor.entrySet()){
            List<Room> rooms = floor.getValue();
            for (int i = 0; i + amount <= rooms.size(); i++){
                List<Room> window = rooms.subList(i, i + amount);
                long[] score = {
                        cartFloors.contains(floor.getKey()) ? 0 : 1,
                        window.getLast().getRoomNumber() - window.getFirst().getRoomNumber(),
                        distanceToCart(window, cartRoomNumbers)
                };
                if (best == null || Arrays.compare(score, bestScore) < 0){
                    best = window;
                    bestScore = score;
                }
            }
        }
        if (best != null) return List.copyOf(best);

        // No single floor has enough: start on the floor with the most free rooms
        // (cart floor, then lowest floor, on ties), then fill up from the nearest floors
        int startFloor = roomsByFloor.entrySet().stream()
                .min(Comparator.<Map.Entry<Integer, List<Room>>>comparingInt(floor -> -floor.getValue().size())
                        .thenComparingInt(floor -> cartFloors.contains(floor.getKey()) ? 0 : 1)
                        .thenComparingInt(Map.Entry::getKey))
                .orElseThrow()
                .getKey();

        List<Room> picked = new ArrayList<>();
        List<Integer> floorsByDistance = roomsByFloor.keySet().stream()
                .sorted(Comparator.comparingInt(floor -> Math.abs(floor - startFloor)))
                .toList();
        for (int floor : floorsByDistance){
            for (Room room : roomsByFloor.get(floor)){
                if (picked.size() == amount) return picked;
                picked.add(room);
            }
        }
        return picked;
    }

    /**
     * @param roomNumber the room number
     * @return the floor the room is on
     */
    public static int floorOf(int roomNumber){
        return roomNumber / 100;
    }

    /**
     * @param rooms           the rooms to measure from
     * @param cartRoomNumbers the room numbers already in the cart
     * @return the smallest distance between any of the rooms and any room in the cart, or 0 if the cart is empty
     */
    private static long distanceToCart(List<Room> rooms, Collection<Integer> cartRoomNumbers){
        if (cartRoomNumbers.isEmpty()) return 0;
        long min = Long.MAX_VALUE;
        for (Room room : rooms){
            for (int cartRoomNumber : cartRoomNumbers){
                min = Math.min(min, Math.abs(room.getRoomNumber() - cartRoomNumber));
            }
        }
        return min;
    }
}
