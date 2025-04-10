package org.swyp.dessertbee.statistics.store.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.statistics.store.entity.DessertMateLog;

@Repository
public interface DessertMateLogRepository extends JpaRepository<DessertMateLog, Long> {
}