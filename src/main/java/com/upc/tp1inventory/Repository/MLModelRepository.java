package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.MLModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MLModelRepository extends JpaRepository<MLModel, UUID> {

    List<MLModel> findByModelNameOrderByCreatedAtDesc(String modelName);

    Optional<MLModel> findFirstByModelNameAndIsActiveTrueOrderByCreatedAtDesc(String modelName);
}
