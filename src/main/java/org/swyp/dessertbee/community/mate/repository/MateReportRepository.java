package org.swyp.dessertbee.community.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.mate.entity.MateReport;

@Repository
public interface MateReportRepository extends JpaRepository<MateReport, Long> {

    MateReport findByMateIdAndUserId(Long mateId, Long userId);

    MateReport findByMateReplyIdAndUserId(Long replyId, Long userId);
}
