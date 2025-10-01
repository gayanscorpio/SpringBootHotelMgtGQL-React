package com.user.dgs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.dgs.model.User;

public interface UserRepository extends JpaRepository<User, String> {

}
