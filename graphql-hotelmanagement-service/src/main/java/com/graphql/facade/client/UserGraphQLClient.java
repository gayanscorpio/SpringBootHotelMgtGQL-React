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
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.graphql.facade.model.CustomeUser;

@Service
@AllArgsConstructor
public class UserGraphQLClient {
	private static final Logger log = LoggerFactory.getLogger(UserGraphQLClient.class);

	private final GraphQLServiceClient serviceClient;
	private final ObjectMapper mapper = new ObjectMapper();

	private final Cache<String, CustomeUser> userCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(500).build();

	// ✅ Fetch multiple users safely
	@CircuitBreaker(name = "userService", fallbackMethod = "getUsersByIdsFallback")
	public Map<String, Object> getUsersByIds(Set<String> userIds) throws Exception {
		log.info("Fetching users for IDs: {}", userIds);

		if (userIds.isEmpty()) {
			log.warn("No user IDs provided. Returning empty list.");
			return Map.of("data", Map.of("usersByIds", List.of()));
		}

		Map<String, Object> resultMap = new HashMap<>();
		List<CustomeUser> users = new ArrayList<>();
		Set<String> missingIds = new HashSet<>();

		// ✅ Check cache first
		for (String id : userIds) {
			CustomeUser u = userCache.getIfPresent(id);
			if (u != null) {
				log.debug("Cache hit for userId={}", id);
				users.add(u);
			} else {
				log.debug("Cache miss for userId={}", id);
				missingIds.add(id);
			}
		}

		// ✅ Fetch missing users via GraphQL
		if (!missingIds.isEmpty()) {
			log.info("Fetching {} missing users from USER-SERVICE", missingIds.size());
			WebClientGraphQLClient client = serviceClient.getClient("USER-SERVICE");

			StringBuilder idsArray = new StringBuilder("[");
			for (String id : missingIds) {
				idsArray.append("\"").append(escape(id)).append("\",");
			}
			idsArray.deleteCharAt(idsArray.length() - 1);
			idsArray.append("]");

			String query = "{ usersByIds(ids: " + idsArray + ") { id name email } }";
			log.debug("Executing GraphQL query: {}", query);

			GraphQLResponse resp = client.reactiveExecuteQuery(query).block();
			log.debug("Received raw GraphQL response: {}", resp.getJson());

			Map<String, Object> respMap = mapper.readValue(resp.getJson(), new TypeReference<Map<String, Object>>() {
			});

			Map<String, Object> data = (Map<String, Object>) respMap.get("data");
			List<CustomeUser> fetchedUsers = mapper.convertValue(data.get("usersByIds"), new TypeReference<List<CustomeUser>>() {
			});
			log.info("Fetched {} users from USER-SERVICE", fetchedUsers.size());

			// ✅ Put fetched users in cache
			fetchedUsers.forEach(u -> {
				log.debug("Caching userId={} name={}", u.getId(), u.getName());
				userCache.put(u.getId(), u);
			});

			users.addAll(fetchedUsers);
		}

		resultMap.put("data", Map.of("usersByIds", users));
		log.info("Returning {} users", users.size());

		return resultMap;
	}

	public Map<String, Object> getUsersByIdsFallback(Set<String> userIds, Throwable t) {
		log.warn("[CIRCUIT-BREAKER] Fallback triggered for getUsersByIds. Failed to fetch users: {}. Cause: {}",
				userIds, t.toString(), t);

		// ✅ Optionally check if some users exist in cache
		List<CustomeUser> cachedUsers = new ArrayList<>();
		for (String id : userIds) {
			CustomeUser u = userCache.getIfPresent(id);
			if (u != null) {
				cachedUsers.add(u);
			}
		}
		if (!cachedUsers.isEmpty()) {
			log.info("Returning {} cached users during fallback for IDs {}", cachedUsers.size(), userIds);
			return Map.of("data", Map.of("usersByIds", cachedUsers));
		}

		// ✅ Default safe fallback (empty list)
		return Map.of("data", Map.of("usersByIds", List.of()));
	}

	private String escape(String input) {
		return input.replace("\"", "\\\"");
	}

}
