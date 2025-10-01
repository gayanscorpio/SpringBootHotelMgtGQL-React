package com.hotel.dgs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.dgs.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, String> {
	List<Hotel> findByIdIn(List<String> ids);

}
