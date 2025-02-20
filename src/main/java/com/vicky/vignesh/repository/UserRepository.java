package com.vicky.vignesh.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vicky.vignesh.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
