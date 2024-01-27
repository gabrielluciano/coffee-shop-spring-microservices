package com.gabrielluciano.authorizationserver.repository;

import com.gabrielluciano.authorizationserver.model.Client;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client, Long> {

    Optional<Client> findByClientId(String clientId);
}
