package com.graphql.facade.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookingGraphQLClient {
	private static final Logger log = LoggerFactory.getLogger(BookingGraphQLClient.class);

	private final GraphQLServiceClient serviceClient;
	private final ObjectMapper mapper = new ObjectMapper();

	// ✅ Separate caches for different query patterns
	private final Cache<String, Map<String, Object>> bookingByIdCache = Caffeine.newBuilder()
			.expireAfterWrite(30, TimeUnit.MINUTES) // bookings rarely change
			.maximumSize(5000).build();

	private final Cache<String, Map<String, Object>> bookingsByUserCache = Caffeine.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES) // user may book more frequently
			.maximumSize(2000).build();

	/**
	 * // ❌ allBookings: do NOT cache, too dynamic
	 * 
	 * @return
	 * @throws Exception
	 */
	@CircuitBreaker(name = "bookingService", fallbackMethod = "getAllBookingsFallback")
	public Map<String, Object> getAllBookings() throws Exception {
		log.info("Fetching all bookings");
		WebClientGraphQLClient client = serviceClient.getClient("BOOKING-SERVICE");
		String query = "{ allBookings { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }";

		// Block to get the response synchronously
		GraphQLResponse response = client.reactiveExecuteQuery(query).block();

		if (response == null) {
			log.error("No response from booking service");
			throw new RuntimeException("No response from booking service");
		}
		Map<String, Object> result = mapper.readValue(response.getJson(), new TypeReference<>() {
		});
		log.info("Fetched {} bookings", ((Map<?, ?>) result.get("data")).size());
		return result;
	}

	public Map<String, Object> getAllBookingsFallback(Throwable t) {
		log.warn("Fallback for getAllBookings triggered: {}", t.getMessage());
		Map<String, Object> result = new HashMap<>();
		result.put("data", Map.of("allBookings", List.of()));
		return result;
	}

	/**
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@CircuitBreaker(name = "bookingService", fallbackMethod = "getBookingsByUserFallback")
	public Map<String, Object> getBookingsByUser(String userId) throws Exception {
		log.info("Fetching bookings for userId={}", userId);

		return bookingsByUserCache.get(userId, key -> {
			try {
				WebClientGraphQLClient client = serviceClient.getClient("BOOKING-SERVICE");
				String query = "{ bookingsByUser(userId: \"" + escape(userId)
						+ "\") { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }";
				GraphQLResponse resp = client.reactiveExecuteQuery(query).block();
				String json = resp.getJson();
				log.info("Fetched bookings for userId={}", userId);

				return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
				});
			} catch (Exception e) {
				log.error("Error fetching bookings for userId={}", userId, e);
				throw new RuntimeException(e);
			}
		});
	}

	public Map<String, Object> getBookingsByUserFallback(String userId, Throwable t) {
		log.warn("Fallback for getBookingsByUser triggered for userId={}: {}", userId, t.getMessage());

		Map<String, Object> result = new HashMap<>();
		result.put("data", Map.of("bookingsByUser", List.of()));
		return result;
	}

	@CircuitBreaker(name = "bookingService", fallbackMethod = "getBookingByIdFallback")
	public Map<String, Object> getBookingById(String id) throws Exception {
		log.info("Fetching booking by id={}", id);

		return bookingByIdCache.get(id, key -> {
			try {
				WebClientGraphQLClient client = serviceClient.getClient("BOOKING-SERVICE");

				// GraphQL query for a single booking
				String query = "{ bookingById(id: \"" + escape(id) + "\") "
						+ "{ id userId hotelId roomId checkInDate checkOutDate status totalPrice } }";

				GraphQLResponse resp = client.reactiveExecuteQuery(query).block();
				String json = resp.getJson();
				log.info("Fetched booking for id={}", id);

				return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
				});
			} catch (Exception e) {
				log.error("Error fetching booking by id={}", id, e);
				throw new RuntimeException(e);
			}
		});
	}

	public Map<String, Object> getBookingByIdFallback(String id, Throwable t) {
		log.warn("Fallback for getBookingById triggered for id={}: {}", id, t.getMessage());

		Map<String, Object> result = new HashMap<>();
		result.put("data", Map.of("bookingById", List.of()));
		return result;
	}

	private String escape(String input) {
		return input.replace("\"", "\\\"");
	}

	@CircuitBreaker(name = "bookingService", fallbackMethod = "createBookingFallback")
	public Map<String, Object> createBooking(String userId, String hotelId, String roomId, String checkInDate,
			String checkOutDate, Double totalPrice) throws Exception {
		log.info("Creating booking for userId={} hotelId={}", userId, hotelId);
		WebClientGraphQLClient client = serviceClient.getClient("BOOKING-SERVICE");

		String mutation = String.format(
				"mutation { createBooking(userId: \"%s\", hotelId: \"%s\", roomId: \"%s\", "
						+ "checkInDate: \"%s\", checkOutDate: \"%s\", totalPrice: %s) "
						+ "{ id userId hotelId roomId checkInDate checkOutDate status totalPrice } }",
				escape(userId), escape(hotelId), roomId != null ? escape(roomId) : "", escape(checkInDate),
				escape(checkOutDate), totalPrice);

		GraphQLResponse resp = client.reactiveExecuteQuery(mutation).block();
		if (resp == null) {
			log.error("No response from booking service for createBooking");
			throw new RuntimeException("No response from booking service");
		}
		log.info("Booking created successfully for userId={} hotelId={}", userId, hotelId);
		return mapper.readValue(resp.getJson(), new TypeReference<>() {
		});

	}

	public Map<String, Object> createBookingFallback(String userId, String hotelId, String roomId, String checkInDate,
			String checkOutDate, Double totalPrice, Throwable t) {
		log.warn("Fallback for createBooking triggered for userId={}, hotelId={}: {}", userId, hotelId, t.getMessage());

		Map<String, Object> result = new HashMap<>();
		result.put("data", Map.of("createBooking", null));
		return result;
	}

	@CircuitBreaker(name = "bookingService", fallbackMethod = "cancelBookingFallback")
	public Map<String, Object> cancelBooking(String id) throws Exception {
		log.info("Cancelling booking id={}", id);
		WebClientGraphQLClient client = serviceClient.getClient("BOOKING-SERVICE");

		String mutation = String.format(
				"mutation { cancelBooking(id: \"%s\") { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }",
				escape(id));

		GraphQLResponse resp = client.reactiveExecuteQuery(mutation).block();
		if (resp == null) {
			log.error("No response from booking service for cancelBooking id={}", id);
			throw new RuntimeException("No response from booking service");
		}
		log.info("Booking cancelled successfully id={}", id);
		return mapper.readValue(resp.getJson(), new TypeReference<>() {
		});
	}

	public Map<String, Object> cancelBookingFallback(String id, Throwable t) {
		log.warn("Fallback for cancelBooking triggered for id={}: {}", id, t.getMessage());

		Map<String, Object> result = new HashMap<>();
		result.put("data", Map.of("cancelBooking", null));
		return result;
	}

}
