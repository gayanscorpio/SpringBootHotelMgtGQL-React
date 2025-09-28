package com.graphql.facade.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
	private String id;
	private String userId;
	private String hotelId;
	private String roomId;
	private String checkInDate;
	private String checkOutDate;
	private String status;
	private Double totalPrice;

	// Enriched fields
	private User user;
	private Hotel hotel;

	// Enrichment flags
	private boolean userEnriched;
	private boolean hotelEnriched;
}
