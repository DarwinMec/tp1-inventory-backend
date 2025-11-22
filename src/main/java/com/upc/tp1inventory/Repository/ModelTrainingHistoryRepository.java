package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.ModelTrainingHistory;
import com.upc.tp1inventory.Entity.MLModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModelTrainingHistoryRepository extends JpaRepository<ModelTrainingHistory, UUID> {

    List<ModelTrainingHistory> findByModelOrderByTrainingStartDesc(MLModel model);
}
