package com.booking.dgs.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.booking.dgs.model.Booking;
import com.booking.dgs.repository.BookingRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BookingService {

	private final BookingRepository repository;

	public Booking createBooking(Booking booking) {
		// You can generate ID here if not provided
		if (booking.getId() == null) {
			booking.setId(UUID.randomUUID().toString());
		}
		return repository.save(booking);
	}

	public List<Booking> getAllBookings() {
		return repository.findAll();
	}

	public Booking getBookingById(String id) {
		return repository.findById(id).orElse(null);
	}

	public List<Booking> getBookingsByUser(String userId) {
		return repository.findByUserId(userId);
	}

	public Booking cancelBooking(String id) {
		Booking booking = repository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found: " + id));
		booking.setStatus("CANCELLED");
		return repository.save(booking);
	}
}
