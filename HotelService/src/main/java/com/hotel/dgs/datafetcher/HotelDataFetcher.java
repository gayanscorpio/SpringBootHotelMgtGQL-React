package com.hotel.dgs.datafetcher;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hotel.dgs.model.Hotel;
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
	public Hotel hotelById(@InputArgument Long id) {
		return hotelRepository.findById(id).orElse(null);
	}

	// Create new hotel
	@DgsMutation
	public Hotel createHotel(@InputArgument("hotel") Hotel input) {
		return hotelRepository.save(input);
	}
}
