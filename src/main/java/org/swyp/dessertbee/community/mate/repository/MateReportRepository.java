package org.swyp.dessertbee.community.mate.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.mate.entity.MateReport;

import java.util.List;

@Repository
public interface MateReportRepository extends JpaRepository<MateReport, Long> {

    MateReport findByMateIdAndUserId(Long mateId, Long userId);

    MateReport findByMateReplyIdAndUserId(Long replyId, Long userId);

    List<MateReport> findAllByMateIdIsNotNull();

    boolean existsByMateId(Long mateId);

    List<MateReport> findAllByMateReplyIdIsNotNull();

    boolean existsByMateReplyId(Long mateReplyId);

    // 특정 게시글의 전체 신고 횟수
    long countByMateId(Long mateId);

    // 특정 게시글의 카테고리별 신고 횟수
    long countByMateIdAndReportCategoryId(Long mateId, Long reportCategoryId);

    // 특정 게시글의 카테고리별 신고 횟수 일괄 집계 (카테고리별로 그룹핑)
    @Query("SELECT mr.reportCategoryId, COUNT(mr) FROM MateReport mr WHERE mr.mateId = :mateId GROUP BY mr.reportCategoryId")
    List<Object[]> countByMateIdGroupByCategory(@Param("mateId") Long mateId);

    // 특정 댓글의 전체 신고수
    long countByMateReplyId(Long mateReplyId);

    // 댓글에 대한 동일 유형 신고수 카운트
    long countByMateReplyIdAndReportCategoryId(Long mateReplyId, Long reportCategoryId);

    // 특정 댓글의 카테고리별 신고수 집계
    @Query("SELECT mr.reportCategoryId, COUNT(mr) FROM MateReport mr WHERE mr.mateReplyId = :mateReplyId GROUP BY mr.reportCategoryId")
    List<Object[]> countByMateReplyIdGroupByCategory(@Param("mateReplyId") Long mateReplyId);

}

