package com.user.dgs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custome_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomeUser {
	@Id
	private String id;

	private String name;
	private String email;
}
