package com.gabrielluciano.authorizationserver.repository;

import com.gabrielluciano.authorizationserver.model.UserCredentials;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepository extends CrudRepository<UserCredentials, UUID> {

    Optional<UserCredentials> findByEmail(String email);
}
