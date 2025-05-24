package org.swyp.dessertbee.statistics.store.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsHourly;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StoreStatisticsHourlyRepository extends JpaRepository<StoreStatisticsHourly, Long> {
    Optional<StoreStatisticsHourly> findByStoreIdAndDateAndHour(Long storeId, LocalDate date, int hour);
}