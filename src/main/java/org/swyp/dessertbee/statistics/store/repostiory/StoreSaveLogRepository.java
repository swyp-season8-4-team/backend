package org.swyp.dessertbee.statistics.store.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.statistics.store.entity.StoreSaveLog;

@Repository
public interface StoreSaveLogRepository extends JpaRepository<StoreSaveLog, Long> {
}