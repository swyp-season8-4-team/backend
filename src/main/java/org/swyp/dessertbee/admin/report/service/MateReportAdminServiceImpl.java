package org.swyp.dessertbee.admin.report.service;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.admin.report.dto.response.MateReplyReportCountResponse;
import org.swyp.dessertbee.admin.report.dto.response.MateReportCountResponse;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateReply;
import org.swyp.dessertbee.community.mate.entity.MateReport;
import org.swyp.dessertbee.community.mate.exception.MateExceptions;
import org.swyp.dessertbee.community.mate.repository.MateReplyRepository;
import org.swyp.dessertbee.community.mate.repository.MateReportRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;
import org.swyp.dessertbee.community.mate.service.MateReplyService;
import org.swyp.dessertbee.community.mate.service.MateService;
import org.swyp.dessertbee.email.service.WarningMailService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateReportAdminServiceImpl implements MateReportAdminService {

    private final MateRepository mateRepository;
    private final MateReportRepository mateReportRepository;
    private final MateReplyRepository mateReplyRepository;
    private final UserRepository userRepository;
    private final WarningMailService warningMailService;

    /**
     *   신고된 Mate 게시글 목록 조회
     */
    public List<MateReportResponse> getReportedMates() {
        List<MateReport> reports = mateReportRepository.findAllByMateIdIsNotNull();
        return reports.stream()
                .map(MateReportResponse::new)
                .collect(Collectors.toList());
    }


    /**
     * 신고된 Mate(게시글)의 신고 횟수(전체, 카테고리별) 조회
     */
    @Transactional(readOnly = true)
    public MateReportCountResponse getMateReportCount(UUID mateUuid) {
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateExceptions.MateNotFoundException("존재하지 않는 디저트메이트입니다."));
        Long mateId = mate.getMateId();

        // 전체 신고수
        long totalCount = mateReportRepository.countByMateId(mateId);

        // 카테고리별 신고수
        List<Object[]> grouped = mateReportRepository.countByMateIdGroupByCategory(mateId);
        Map<Long, Long> categoryCounts = new HashMap<>();
        for (Object[] row : grouped) {
            Long categoryId = (Long) row[0];
            Long count = (Long) row[1];
            categoryCounts.put(categoryId, count);
        }

        return MateReportCountResponse.builder()
                .mateId(mateId)
                .totalReportCount(totalCount)
                .categoryReportCounts(categoryCounts)
                .build();
    }

    /**
     * 신고된 Mate 게시글 삭제 (1단계)
     */
    @Transactional
    public void deleteMateByUuid(UUID mateUuid) {
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateExceptions.MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        // mateUuid가 있는 신고 데이터 확인
        boolean isReported = mateReportRepository.existsByMateId(mate.getMateId());
        if (!isReported) {
            throw new MateExceptions.MateReportNotFoundException("신고되지 않은 디저트메이트입니다.");
        }

        // 게시글 삭제 (soft delete)
        mate.softDelete();
        mateRepository.save(mate);
    }

    /**
     * 신고된 Mate 게시글 사용자 경고 (2단계)
     */
    @Transactional
    public void warnMateAuthor(UUID mateUuid,Long reportCategoryId) {
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateExceptions.MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        // 동일 신고 유형(카테고리) 3회 이상인지 확인
        long count = mateReportRepository.countByMateIdAndReportCategoryId(mate.getMateId(), reportCategoryId);
        if (count < 3) {
            throw new MateExceptions.MateReportNotFoundException( "동일 유형 신고가 3회 미만입니다.");
        }

        //작성자 정보 조회
        UserEntity user = userRepository.findById(mate.getUserId())
                .orElseThrow(() -> new MateExceptions.MateReportNotFoundException("작성자 정보를 찾을 수 없습니다."));

        // 경고 메일 발송
        String reason = getReportCategoryName(reportCategoryId); // 신고 유형명 반환 메서드 구현 필요
        warningMailService.sendWarningEmail(user.getEmail(), reason);
    }


    /**
     *  신고된 Mate 댓글 조회
     */
    public List<MateReportResponse> getReportedMateReplies() {
        List<MateReport> reports = mateReportRepository.findAllByMateReplyIdIsNotNull();

        return reports.stream()
                .map(MateReportResponse::new)
                .collect(Collectors.toList());
    }


    /**
     * 신고된 Mate 댓글의 신고 횟수(전체, 카테고리별) 조회
     */
    @Transactional(readOnly = true)
    public MateReplyReportCountResponse getMateReplyReportCount(Long mateReplyId) {
        // 전체 신고수
        long totalCount = mateReportRepository.countByMateReplyId(mateReplyId);

        // 카테고리별 신고수
        List<Object[]> grouped = mateReportRepository.countByMateReplyIdGroupByCategory(mateReplyId);
        Map<Long, Long> categoryCounts = new HashMap<>();
        for (Object[] row : grouped) {
            Long categoryId = (Long) row[0];
            Long count = (Long) row[1];
            categoryCounts.put(categoryId, count);
        }

        return MateReplyReportCountResponse.builder()
                .mateReplyId(mateReplyId)
                .totalReportCount(totalCount)
                .categoryReportCounts(categoryCounts)
                .build();
    }

    /**
     * 신고된 Mate 댓글 삭제 (1단계)
     */
    @Transactional
    public void deleteReportedMateReply(Long mateReplyId) {
        boolean isReported = mateReportRepository.existsByMateReplyId(mateReplyId);
        if (!isReported) {
            throw new MateExceptions.MateReplyNotReportedException("신고되지 않은 디저트메이트 댓글입니다.");
        }

        MateReply mateReply = mateReplyRepository.findByMateReplyIdAndDeletedAtIsNull(mateReplyId)
                .orElseThrow(() ->  new MateExceptions.MateReplyNotReportedException("신고되지 않은 디저트메이트 댓글입니다."));

        mateReply.softDelete();
        mateReplyRepository.save(mateReply);
    }

    /**
     * 신고된 Mate 댓글 작성자 경고 (2단계)
     */
    @Transactional
    public void warnMateReplyAuthor(Long mateReplyId, Long reportCategoryId) {
        // 동일 유형 신고 3회 이상인지 확인
        long count = mateReportRepository.countByMateReplyIdAndReportCategoryId(mateReplyId, reportCategoryId);
        if (count < 3) {
            throw new MateExceptions.MateReportNotFoundException("동일 유형 신고가 3회 미만입니다.");
        }

        // 댓글 정보 조회
        MateReply mateReply = mateReplyRepository.findByMateReplyIdAndDeletedAtIsNull(mateReplyId)
                .orElseThrow(() -> new MateExceptions.MateReplyNotReportedException("존재하지 않는 댓글입니다."));

        // 작성자 정보 조회
        UserEntity user = userRepository.findById(mateReply.getUserId())
                .orElseThrow(() -> new MateExceptions.MateReportNotFoundException("작성자 정보를 찾을 수 없습니다."));

        // 경고 메일 발송
        String reason = getReportCategoryName(reportCategoryId); // 신고 유형명 반환
        warningMailService.sendWarningEmail(user.getEmail(), reason);
    }

    // 신고 유형명을 반환하는 메서드
    private String getReportCategoryName(Long reportCategoryId) {
        // 예시: 실제로는 DB 또는 Enum 등에서 조회
        switch (reportCategoryId.intValue()) {
            case 1: return "욕설 및 폭언";
            case 2: return "음란물 또는 부적절한 내용";
            case 3: return "광고 또는 스팸";
            case 4: return "허위 정보";
            case 5: return "게시물 도배";
            case 6: return "기타";
            default: return "기타";
        }
    }
}

