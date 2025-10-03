package com.user.dgs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.dgs.model.CustomeUser;

public interface UserRepository extends JpaRepository<CustomeUser, String> {

}
