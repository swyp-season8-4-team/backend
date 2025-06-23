package org.swyp.dessertbee.statistics.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsPeriodic;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoreStatisticsPeriodRepository extends JpaRepository<StoreStatisticsPeriodic, Long> {

    Optional<StoreStatisticsPeriodic> findByStoreIdAndDateAndPeriodType(Long storeId, LocalDate date, PeriodType type);

    List<StoreStatisticsPeriodic> findByStoreIdAndDateBetweenAndPeriodType(Long storeId, LocalDate start, LocalDate end, PeriodType periodType);

}
