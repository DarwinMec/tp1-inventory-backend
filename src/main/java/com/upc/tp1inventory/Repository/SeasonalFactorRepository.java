package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.SeasonalFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonalFactorRepository extends JpaRepository<SeasonalFactor, UUID> {

    List<SeasonalFactor> findByMonthOrderByDayOfWeekAsc(Integer month);

    Optional<SeasonalFactor> findByMonthAndDayOfWeek(Integer month, Integer dayOfWeek);
}
