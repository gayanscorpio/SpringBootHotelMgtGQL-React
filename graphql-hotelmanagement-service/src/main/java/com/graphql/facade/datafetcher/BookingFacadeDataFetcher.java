package com.graphql.facade.datafetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.facade.client.BookingGraphQLClient;
import com.graphql.facade.client.HotelGraphQLClient;
import com.graphql.facade.client.UserGraphQLClient;
import com.graphql.facade.model.Booking;
import com.graphql.facade.model.Hotel;
import com.graphql.facade.model.User;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;

import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class BookingFacadeDataFetcher {

	private static final Logger log = LoggerFactory.getLogger(BookingGraphQLClient.class);

	private final BookingGraphQLClient bookingClient;
	private final HotelGraphQLClient hotelClient;
	private final UserGraphQLClient userClient;

	private final ObjectMapper mapper = new ObjectMapper();

	@DgsQuery
	public List<Booking> allBookings() throws Exception {
		Map<String, Object> resp = bookingClient.getAllBookings();
		return enrichBookings(resp, "allBookings");
	}

	/**
	 * 
	 * @param bookingResponse - bookingResponse is expected to be the raw GraphQL
	 *                        response parsed as Map<String,Object>
	 * @param key             - key is the GraphQL field name to extract (like
	 *                        "allBookings" or "bookingsByUser").
	 * @return- Returns a List<Booking>.
	 */
	@SuppressWarnings("unchecked")
	private List<Booking> enrichBookings(Map<String, Object> bookingResponse, String key) {
		if (bookingResponse == null) {
			log.warn("Booking response was null for key={}", key);
			return List.of();
		}

		Map<String, Object> data = (Map<String, Object>) bookingResponse.get("data");
		if (data == null || data.get(key) == null) {
			log.warn("No data found in booking response for key={}", key);
			return List.of();
		}

		// Convert response to list of Booking
		List<Booking> bookings = mapper.convertValue(data.get(key), new TypeReference<List<Booking>>() {
		});
		log.info("Fetched {} bookings for key={} → {}", bookings.size(), key, bookings);

		if (bookings.isEmpty())
			return bookings;

		// ✅ Collect IDs for batch fetching
		Set<String> userIds = bookings.stream().map(Booking::getUserId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Set<String> hotelIds = bookings.stream().map(Booking::getHotelId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<String, User> userMap = new HashMap<>();
		Map<String, Hotel> hotelMap = new HashMap<>();

		// ✅ Batch fetch users
		try {
			Map<String, Object> userResp = userClient.getUsersByIds(userIds); // GraphQL call
			Map<String, Object> userData = (Map<String, Object>) userResp.get("data");
			List<User> users = mapper.convertValue(userData.get("usersByIds"), new TypeReference<List<User>>() {
			});
			users.forEach(u -> userMap.put(u.getId(), u));
			log.info("users {}", users);
		} catch (Exception ex) {
			log.warn("User enrichment failed for booking batch {}", userIds, ex);
		}

		// ✅ Batch fetch hotels
		try {
			Map<String, Object> hotelResp = hotelClient.getHotelsByIds(hotelIds); // GraphQL call
			Map<String, Object> hotelData = (Map<String, Object>) hotelResp.get("data");
			List<Hotel> hotels = mapper.convertValue(hotelData.get("hotelsByIds"), new TypeReference<List<Hotel>>() {
			});
			hotels.forEach(h -> hotelMap.put(h.getId(), h));
			log.info("hotels {}", hotels);
		} catch (Exception ex) {
			log.warn("Hotel enrichment failed for booking batch {}", hotelIds, ex);
		}

		// ✅ Enrich bookings
		List<Booking> enriched = bookings.stream().map(b -> {
			User u = userMap.get(b.getUserId());
			Hotel h = hotelMap.get(b.getHotelId());

			b.setUser(u);
			b.setUserEnriched(u != null);

			b.setHotel(h);
			b.setHotelEnriched(h != null);

			return b;
		}).collect(Collectors.toList());
		log.info("Enriched bookings → {}", enriched);

		return enriched;
	}

	/**
	 * getBookingById calls bookingById(id: "...") → returns single object.
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@DgsQuery
	public Booking bookingById(@InputArgument String id) throws Exception {
		log.info("Fetching booking by id={}", id);

		Map<String, Object> resp = bookingClient.getBookingById(id);
		List<Booking> bookings = enrichBookings(resp, "bookingById");
		Booking result = bookings.isEmpty() ? null : bookings.get(0);

		log.info("Result bookingById id={} → {}", id, result);
		return result;
	}

	/**
	 * getBookingsByUser calls bookingsByUser(userId: "...") → returns list.
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@DgsQuery
	public List<Booking> bookingsByUser(@InputArgument String userId) throws Exception {
		log.info("Fetching bookings by userId={}", userId);

		Map<String, Object> resp = bookingClient.getBookingsByUser(userId);
		List<Booking> result = enrichBookings(resp, "bookingsByUser");
		log.info("Result bookingsByUser userId={} → {}", userId, result);
		return result;
	}
}
