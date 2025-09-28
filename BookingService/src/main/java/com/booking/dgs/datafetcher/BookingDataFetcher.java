package com.booking.dgs.datafetcher;

import java.util.List;

import com.booking.dgs.model.Booking;
import com.booking.dgs.service.BookingService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;

import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class BookingDataFetcher {

	private final BookingService bookingService;

	@DgsMutation
	public Booking createBooking(@InputArgument Booking booking) {
		return bookingService.createBooking(booking);
	}

	@DgsQuery
	public List<Booking> allBookings() {
		return bookingService.getAllBookings();
	}

	@DgsQuery
	public Booking bookingById(@InputArgument String id) {
		return bookingService.getBookingById(id);
	}

	@DgsQuery
	public List<Booking> bookingsByUser(@InputArgument String userId) {
		return bookingService.getBookingsByUser(userId);
	}
}
