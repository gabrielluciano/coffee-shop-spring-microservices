package com.gabrielluciano.userservice.repository;

import com.gabrielluciano.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
