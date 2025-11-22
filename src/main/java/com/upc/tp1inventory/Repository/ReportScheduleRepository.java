package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID> {

    List<ReportSchedule> findByIsActiveTrue();
}
