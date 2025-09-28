package com.booking.dgs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
	@Id
	private String id;

	private String userId;
	private String hotelId;
	private String roomId;
	private String checkInDate;
	private String checkOutDate;
	private String status;
	private Double totalPrice;
}
