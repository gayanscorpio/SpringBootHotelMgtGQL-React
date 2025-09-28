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
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.graphql.facade.model.User;

@Service
@AllArgsConstructor
public class UserGraphQLClient {

	private final GraphQLServiceClient serviceClient;
	private final ObjectMapper mapper = new ObjectMapper();

	private final Cache<String, User> userCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(500).build();

	// ✅ Fetch multiple users safely
	@CircuitBreaker(name = "userService", fallbackMethod = "getUsersByIdsFallback")
	public Map<String, Object> getUsersByIds(Set<String> userIds) throws Exception {
		if (userIds.isEmpty()) {
			return Map.of("data", Map.of("usersByIds", List.of()));
		}

		Map<String, Object> resultMap = new HashMap<>();
		List<User> users = new ArrayList<>();
		Set<String> missingIds = new HashSet<>();

		// ✅ Check cache first
		for (String id : userIds) {
			User u = userCache.getIfPresent(id);
			if (u != null) {
				users.add(u);
			} else {
				missingIds.add(id);
			}
		}

		// ✅ Fetch missing users via GraphQL
		if (!missingIds.isEmpty()) {
			WebClientGraphQLClient client = serviceClient.getClient("USER-SERVICE");

			StringBuilder idsArray = new StringBuilder("[");
			for (String id : missingIds) {
				idsArray.append("\"").append(escape(id)).append("\",");
			}
			idsArray.deleteCharAt(idsArray.length() - 1);
			idsArray.append("]");

			String query = "{ usersByIds(ids: " + idsArray + ") { id name email } }";

			GraphQLResponse resp = client.reactiveExecuteQuery(query).block();
			Map<String, Object> respMap = mapper.readValue(resp.getJson(), new TypeReference<Map<String, Object>>() {
			});

			Map<String, Object> data = (Map<String, Object>) respMap.get("data");
			List<User> fetchedUsers = mapper.convertValue(data.get("usersByIds"), new TypeReference<List<User>>() {
			});

			// ✅ Put fetched users in cache
			fetchedUsers.forEach(u -> userCache.put(u.getId(), u));

			users.addAll(fetchedUsers);
		}

		resultMap.put("data", Map.of("usersByIds", users));
		return resultMap;
	}

	public Map<String, Object> getUsersByIdsFallback(Set<String> userIds, Throwable t) {
		Map<String, Object> result = new HashMap<>();
		result.put("data", Map.of("usersByIds", List.of())); // empty list fallback
		return result;
	}

	private String escape(String input) {
		return input.replace("\"", "\\\"");
	}

}
