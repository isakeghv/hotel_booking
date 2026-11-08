package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingItem;

import java.util.List;

/**
 * A booking with its rooms and extras, as saved or loaded together.
 *
 * @param booking the booking
 * @param items   its rooms
 * @param addons  the extras on its rooms
 */
public record BookingDetails(Booking booking, List<BookingItem> items, List<BookingAddon> addons) {}
