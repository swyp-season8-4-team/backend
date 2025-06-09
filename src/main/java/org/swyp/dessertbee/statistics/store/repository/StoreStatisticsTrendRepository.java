package org.swyp.dessertbee.statistics.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsTrend;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;

import java.time.LocalDate;
import java.util.List;

public interface StoreStatisticsTrendRepository extends JpaRepository<StoreStatisticsTrend, Long> {

    List<StoreStatisticsTrend> findByStoreIdAndDateAndPeriodType(Long storeId, LocalDate date, PeriodType periodType);

    List<StoreStatisticsTrend> findByStoreIdAndDateBetweenAndPeriodType(Long storeId, LocalDate start, LocalDate end, PeriodType periodType);

}
