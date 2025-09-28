package com.hotel.dgs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class HotelMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelMainApplication.class, args);

	}

}
