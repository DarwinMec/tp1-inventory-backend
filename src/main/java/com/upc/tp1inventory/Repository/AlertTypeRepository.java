package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertTypeRepository extends JpaRepository<AlertType, UUID> {

    Optional<AlertType> findByName(String name);
}
