package com.booking.dgs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.booking.dgs.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
	List<Booking> findByUserId(String userId);
}
