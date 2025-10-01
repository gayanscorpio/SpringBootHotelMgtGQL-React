package com.hotel.dgs.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

	@Id
	private String id; // ✅ No auto-generation, we set it manually

	private String name;

	private String city;

	private String address;

	private Integer stars; // optional rating
}
