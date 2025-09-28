package com.graphql.facade.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

	private final GraphQLServiceClient serviceClient;
	private final ObjectMapper mapper = new ObjectMapper();

	// ✅ Caffeine cache for hotels
	private final Cache<String, Hotel> hotelCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(500).build();

	// ✅ Fetch multiple hotels
	@CircuitBreaker(name = "hotelService", fallbackMethod = "getHotelsByIdsFallback")
	public Map<String, Object> getHotelsByIds(Set<String> hotelIds) throws Exception {
		if (hotelIds.isEmpty()) {
			return Map.of("data", Map.of("hotelsByIds", List.of()));
		}

		Map<String, Object> resultMap = new HashMap<>();
		List<Hotel> hotels = new ArrayList<>();

		Set<String> missingIds = new HashSet<>();

		// ✅ Check cache first
		for (String id : hotelIds) {
			Hotel h = hotelCache.getIfPresent(id);
			if (h != null) {
				hotels.add(h);
			} else {
				missingIds.add(id);
			}
		}

		// ✅ Fetch missing hotels via GraphQL
		if (!missingIds.isEmpty()) {
			WebClientGraphQLClient client = serviceClient.getClient("HOTEL-SERVICE");

			StringBuilder idsArray = new StringBuilder("[");
			for (String id : missingIds) {
				idsArray.append("\"").append(escape(id)).append("\",");
			}
			idsArray.deleteCharAt(idsArray.length() - 1);
			idsArray.append("]");

			String query = "{ hotelsByIds(ids: " + idsArray + ") { id name city } }";

			GraphQLResponse resp = client.reactiveExecuteQuery(query).block();
			Map<String, Object> respMap = mapper.readValue(resp.getJson(), new TypeReference<Map<String, Object>>() {
			});

			Map<String, Object> data = (Map<String, Object>) respMap.get("data");
			List<Hotel> fetchedHotels = mapper.convertValue(data.get("hotelsByIds"), new TypeReference<List<Hotel>>() {
			});

			// ✅ Put fetched hotels in cache
			fetchedHotels.forEach(h -> hotelCache.put(h.getId(), h));

			hotels.addAll(fetchedHotels);
		}

		resultMap.put("data", Map.of("hotelsByIds", hotels));
		return resultMap;
	}

	public Map<String, Object> getHotelsByIdsFallback(Set<String> hotelIds, Throwable t) {
		return Map.of("data", Map.of("hotelsByIds", List.of()));
	}

	// Escape quotes safely
	private String escape(String input) {
		return input.replace("\"", "\\\"");
	}

}
