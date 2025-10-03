package com.graphql.facade.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.graphql.facade.model.Hotel;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class HotelGraphQLClient {
	private static final Logger log = LoggerFactory.getLogger(HotelGraphQLClient.class);

	private final GraphQLServiceClient serviceClient;
	private final ObjectMapper mapper = new ObjectMapper();

	// ✅ Caffeine cache for hotels
	private final Cache<String, Hotel> hotelCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(500).build();

	// ✅ Fetch multiple hotels
	@CircuitBreaker(name = "hotelService", fallbackMethod = "getHotelsByIdsFallback")
	public Map<String, Object> getHotelsByIds(Set<String> hotelIds) throws Exception {
		log.info("Fetching hotels by IDs → {}", hotelIds);

		if (hotelIds.isEmpty()) {
			log.info("No hotel IDs provided, returning empty list");
			return Map.of("data", Map.of("hotelsByIds", List.of()));
		}

		Map<String, Object> resultMap = new HashMap<>();
		List<Hotel> hotels = new ArrayList<>();

		Set<String> missingIds = new HashSet<>();

		// ✅ Check cache first
		for (String id : hotelIds) {
			Hotel cached = hotelCache.getIfPresent(id);
			if (cached != null) {
				log.debug("Cache hit for hotelId={} → {}", id, cached);
				hotels.add(cached);
			} else {
				log.debug("Cache miss for hotelId={}", id);
				missingIds.add(id);
			}
		}

		// ✅ Fetch missing hotels via GraphQL
		if (!missingIds.isEmpty()) {
			log.info("Fetching {} hotels from HOTEL-SERVICE for IDs={}", missingIds.size(), missingIds);
			WebClientGraphQLClient client = serviceClient.getClient("HOTEL-SERVICE");

			StringBuilder idsArray = new StringBuilder("[");
			for (String id : missingIds) {
				idsArray.append("\"").append(escape(id)).append("\",");
			}
			idsArray.deleteCharAt(idsArray.length() - 1);
			idsArray.append("]");

			String query = "{ hotelsByIds(ids: " + idsArray + ") { id name city } }";

			GraphQLResponse resp = client.reactiveExecuteQuery(query).block();
			log.debug("Raw response JSON → {}", resp.getJson());

			Map<String, Object> respMap = mapper.readValue(resp.getJson(), new TypeReference<Map<String, Object>>() {
			});

			Map<String, Object> data = (Map<String, Object>) respMap.get("data");
			List<Hotel> fetchedHotels = mapper.convertValue(data.get("hotelsByIds"), new TypeReference<List<Hotel>>() {
			});
			log.info("Fetched {} hotels from HOTEL-SERVICE → {}", fetchedHotels.size(), fetchedHotels);

			// ✅ Put fetched hotels in cache
			fetchedHotels.forEach(h -> {
				hotelCache.put(h.getId(), h);
				log.debug("Cached hotel → {}", h);
			});

			hotels.addAll(fetchedHotels);
		}

		resultMap.put("data", Map.of("hotelsByIds", hotels));
		return resultMap;
	}

	/**
	 * Fallback method when HOTEL-SERVICE is down.
	 */
	public Map<String, Object> getHotelsByIdsFallback(Set<String> hotelIds, Throwable t) {
		log.error("Fallback triggered for hotelIds={} due to error={}", hotelIds, t.getMessage(), t);
		return Map.of("data", Map.of("hotelsByIds", List.of()));
	}

	/**
	 * Escapes quotes in strings for safe GraphQL query building.
	 */
	private String escape(String input) {
		String escaped = input.replace("\"", "\\\"");
		log.debug("Escaped input → {} → {}", input, escaped);
		return escaped;
	}

}
