package com.hotel.dgs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.dgs.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

}
