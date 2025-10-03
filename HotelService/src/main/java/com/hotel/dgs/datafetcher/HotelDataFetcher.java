package com.hotel.dgs.datafetcher;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger log = LoggerFactory.getLogger(HotelDataFetcher.class);

	private final HotelRepository hotelRepository;

	// Fetch all hotels
	@DgsQuery
	public List<Hotel> allHotels() {
		log.info("Fetching all hotels...");
		List<Hotel> hotels = hotelRepository.findAll();
		log.info("Found {} hotels", hotels.size());
		return hotels;
	}

	// Fetch hotel by ID
	@DgsQuery
	public Hotel hotelById(@InputArgument String id) {
		log.info("Fetching hotel by id={}", id);
		Hotel hotel = hotelRepository.findById(id).orElse(null);
		if (hotel != null) {
			log.info("Found hotel: {}", hotel);
		} else {
			log.warn("No hotel found with id={}", id);
		}
		return hotel;
	}

	// lets GraphQL receive an array of hotel IDs.
	@DgsQuery
	public List<Hotel> hotelsByIds(@InputArgument List<String> ids) {
		log.info("Fetching hotels by ids={}", ids);
		List<Hotel> hotels = hotelRepository.findByIdIn(ids);
		log.info("Found {} hotels for ids={}", hotels.size(), ids);
		return hotels;
	}

	// Create new hotel
	@DgsMutation
	public Hotel createHotel(@InputArgument HotelInput hotel) {
		log.info("Creating new hotel with input={}", hotel);

		Hotel newHotel = Hotel.builder().id(hotel.getId() != null ? hotel.getId() : UUID.randomUUID().toString())
				.name(hotel.getName()).city(hotel.getCity()).address(hotel.getAddress()).stars(hotel.getStars())
				.build();

		Hotel savedHotel = hotelRepository.save(newHotel);
		log.info("Hotel created successfully: {}", savedHotel);
		return savedHotel;
	}
}
