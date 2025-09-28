package com.graphql.facade.client;

import java.util.List;
import java.util.Random;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class GraphQLServiceClient {

	private final DiscoveryClient discoveryClient;

	private final Random random = new Random();

	/**
	 * Returns a GraphQLClient pointing to one instance of the requested service.
	 * Service id should match the name in spring.application.name of downstream
	 * services (Eureka registered).
	 */

	public WebClientGraphQLClient getClient(String serviceId) {
		List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
		if (instances == null || instances.isEmpty()) {
			throw new RuntimeException("No instances available for service: " + serviceId);
		}

		ServiceInstance chosen = instances.get(random.nextInt(instances.size()));
		String base = chosen.getUri().toString();
		String graphqlUrl = base.endsWith("/") ? base + "graphql" : base + "/graphql";

		WebClient webClient = WebClient.builder().baseUrl(graphqlUrl).build();

		return new WebClientGraphQLClient(webClient);
	}

}
