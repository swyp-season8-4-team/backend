package org.swyp.dessertbee.common.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.common.entity.ReportCategory;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<ReportCategory, Long> {

    ReportCategory findByReportCategoryId(@NotNull Long reportCategoryId);
}
