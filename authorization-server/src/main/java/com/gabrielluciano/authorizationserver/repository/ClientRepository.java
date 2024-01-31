package com.gabrielluciano.authorizationserver.repository;

import com.gabrielluciano.authorizationserver.model.Client;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends CrudRepository<Client, UUID> {

    Optional<Client> findByClientId(String clientId);
}
