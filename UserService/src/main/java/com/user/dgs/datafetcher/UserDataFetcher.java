package com.user.dgs.datafetcher;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.user.dgs.model.CustomeUser;
import com.user.dgs.model.UserInput;
import com.user.dgs.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@DgsComponent
@RequiredArgsConstructor
public class UserDataFetcher {
	private static final Logger log = LoggerFactory.getLogger(UserDataFetcher.class);

	private final UserRepository userRepository;

	@DgsQuery
	public List<CustomeUser> allUsers() {
		log.info("Fetching all users");
		List<CustomeUser> users = userRepository.findAll();
		log.info("Result: {} users fetched", users.size());
		return users;
	}

	@DgsQuery
	public CustomeUser userById(@InputArgument String id) {
		log.info("Fetching user by ID: {}", id);
		CustomeUser user = userRepository.findById(id).orElse(null);
		log.info("Result: {}", user);
		return user;
	}

	@DgsQuery
	public List<CustomeUser> usersByIds(@InputArgument List<String> ids) {
		log.info("Fetching users by IDs: {}", ids);
		List<CustomeUser> users = userRepository.findAllById(ids);
		log.info("Result: {} users fetched", users.size());
		return users;
	}

	@DgsMutation
	public CustomeUser createUser(@InputArgument("customeUser") UserInput userInput) {
		log.info("Creating new user: {}", userInput);
		CustomeUser newUser = CustomeUser.builder()
				.id(userInput.getId() != null ? userInput.getId() : UUID.randomUUID().toString())
				.name(userInput.getName()).email(userInput.getEmail()).build();

		CustomeUser savedUser = userRepository.save(newUser);
		log.info("User created successfully: {}", savedUser);
		return savedUser;
	}
}
