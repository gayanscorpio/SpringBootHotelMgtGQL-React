package com.hotel.dgs.datafetcher;

import java.util.List;
import java.util.UUID;

import com.hotel.dgs.model.Hotel;
import com.hotel.dgs.model.HotelInput;
import com.hotel.dgs.repository.HotelRepository;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;

import lombok.RequiredArgsConstructor;

@DgsComponent
@RequiredArgsConstructor
public class HotelDataFetcher {

	private final HotelRepository hotelRepository;

	// Fetch all hotels
	@DgsQuery
	public List<Hotel> allHotels() {
		return hotelRepository.findAll();
	}

	// Fetch hotel by ID
	@DgsQuery
	public Hotel hotelById(@InputArgument String id) {
		return hotelRepository.findById(id).orElse(null);
	}

	// lets GraphQL receive an array of hotel IDs.
	@DgsQuery
	public List<Hotel> hotelsByIds(@InputArgument List<String> ids) {
		return hotelRepository.findByIdIn(ids);
	}

	// Create new hotel
	@DgsMutation
	public Hotel createHotel(@InputArgument HotelInput hotel) {
		Hotel newHotel = Hotel.builder()
	            .id(hotel.getId() != null ? hotel.getId() : UUID.randomUUID().toString()) // use provided ID if exists
				.name(hotel.getName()).city(hotel.getCity()).address(hotel.getAddress()).stars(hotel.getStars())
				.build();
		return hotelRepository.save(newHotel);
	}
}
