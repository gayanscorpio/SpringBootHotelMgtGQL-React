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

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookingGraphQLClient {

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

		WebClientGraphQLClient client = serviceClient.getClient("BOOKING-SERVICE");
		String query = "{ allBookings { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }";

		// Block to get the response synchronously
		GraphQLResponse response = client.reactiveExecuteQuery(query).block();

		if (response == null) {
			throw new RuntimeException("No response from booking service");
		}
		return mapper.readValue(response.getJson(), new TypeReference<Map<String, Object>>() {
		});
	}

	public Map<String, Object> getAllBookingsFallback(Throwable t) {
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
		return bookingsByUserCache.get(userId, key -> {
			try {
				WebClientGraphQLClient client = serviceClient.getClient("BOOKING-SERVICE");
				String query = "{ bookingsByUser(userId: \"" + escape(userId)
						+ "\") { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }";
				GraphQLResponse resp = client.reactiveExecuteQuery(query).block();
				String json = resp.getJson();
				return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public Map<String, Object> getBookingsByUserFallback(String userId, Throwable t) {
		Map<String, Object> result = new HashMap<>();
		result.put("data", Map.of("bookingsByUser", List.of()));
		return result;
	}

	@CircuitBreaker(name = "bookingService", fallbackMethod = "getBookingByIdFallback")
	public Map<String, Object> getBookingById(String id) throws Exception {
		return bookingByIdCache.get(id, key -> {
			try {
				WebClientGraphQLClient client = serviceClient.getClient("BOOKING-SERVICE");

				// GraphQL query for a single booking
				String query = "{ bookingById(id: \"" + escape(id) + "\") "
						+ "{ id userId hotelId roomId checkInDate checkOutDate status totalPrice } }";

				GraphQLResponse resp = client.reactiveExecuteQuery(query).block();
				String json = resp.getJson();
				return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public Map<String, Object> getBookingByIdFallback(String id, Throwable t) {
		Map<String, Object> result = new HashMap<>();
		result.put("data", Map.of("bookingById", List.of()));
		return result;
	}

	private String escape(String input) {
		return input.replace("\"", "\\\"");
	}

}
