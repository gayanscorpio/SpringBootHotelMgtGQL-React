package com.hotel.dgs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelInput {
	private String id;
	private String name;
	private String city;
	private String address;
	private Integer stars;
}