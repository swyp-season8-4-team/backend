package org.swyp.dessertbee.admin.report.service;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.admin.report.dto.response.MateReplyReportCountResponse;
import org.swyp.dessertbee.admin.report.dto.response.MateReportCountResponse;
import org.swyp.dessertbee.admin.report.dto.response.ReportActionResponse;
import org.swyp.dessertbee.admin.user.service.UserAdminService;
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
import org.swyp.dessertbee.user.exception.UserExceptions;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.time.LocalDateTime;
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
    private final UserAdminService userAdminService;

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
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND));
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
     * 1단계 : 신고된 Mate 게시글 삭제
     */
    @Transactional
    public void deleteMateByUuid(UUID mateUuid) {
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND));

        // mateUuid가 있는 신고 데이터 확인
        boolean isReported = mateReportRepository.existsByMateId(mate.getMateId());
        if (!isReported) {
            throw new BusinessException(ErrorCode.MATE_NOT_REPORTED);
        }

        // 게시글 삭제 (soft delete)
        mate.softDelete();
        mateRepository.save(mate);
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
     * 1단계 : 신고된 Mate 댓글 삭제
     */
    @Transactional
    public void deleteReportedMateReply(Long mateReplyId) {
        boolean isReported = mateReportRepository.existsByMateReplyId(mateReplyId);
        if (!isReported) {
            throw new BusinessException(ErrorCode.MATE_REPLY_NOT_REPORTED);
        }

        MateReply mateReply = mateReplyRepository.findByMateReplyIdAndDeletedAtIsNull(mateReplyId)
                .orElseThrow(() ->  new BusinessException(ErrorCode.MATE_REPLY_NOT_FOUND));;

        mateReply.softDelete();
        mateReplyRepository.save(mateReply);
    }

    /** 2단계 : 경고(동일 유형 3회 이상) */
    @Transactional
    public ReportActionResponse warnAuthor(UUID mateUuid, Long mateReplyId, Long reportCategoryId) {
        // 신고 여부 확인
        validateReportedTarget(mateUuid, mateReplyId);

        UUID userUuid = getAuthorUuidByTarget(mateUuid, mateReplyId);

        long count = (mateUuid != null)
                ? mateReportRepository.countByMateIdAndReportCategoryId(
                mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND)).getMateId(),
                reportCategoryId)
                : mateReportRepository.countByMateReplyIdAndReportCategoryId(mateReplyId, reportCategoryId);

        if (count < 3) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_REPORT_COUNT);
        }

        UserEntity user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String reason = getReportCategoryName(reportCategoryId);
        userAdminService.warnUserByUuid(userUuid, reason, warningMailService, user.getEmail());

        return ReportActionResponse.builder()
                .mateUuid(mateUuid)
                .mateReplyId(mateReplyId)
                .userUuid(userUuid)
                .actionAt(LocalDateTime.now())
                .build();
    }

    /** 3단계 : 계정 정지(한달) */
    @Transactional
    public ReportActionResponse suspendAuthor(UUID mateUuid, Long mateReplyId) {
        // 신고 여부 확인
        validateReportedTarget(mateUuid, mateReplyId);

        UUID userUuid = getAuthorUuidByTarget(mateUuid, mateReplyId);
        userAdminService.suspendUserForOneMonthByUuid(userUuid);
        UserEntity user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ReportActionResponse.builder()
                .mateUuid(mateUuid)
                .mateReplyId(mateReplyId)
                .userUuid(userUuid)
                .actionAt(LocalDateTime.now())
                .suspendedUntil(user.getSuspendedUntil())
                .build();
    }

    /** 3단계 : 작성 제한(7일) */
    @Transactional
    public ReportActionResponse restrictAuthorWriting(UUID mateUuid, Long mateReplyId) {
        // 신고 여부 확인
        validateReportedTarget(mateUuid, mateReplyId);

        UUID userUuid = getAuthorUuidByTarget(mateUuid, mateReplyId);
        userAdminService.restrictUserWritingFor7DaysByUuid(userUuid);
        UserEntity user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ReportActionResponse.builder()
                .mateUuid(mateUuid)
                .mateReplyId(mateReplyId)
                .userUuid(userUuid)
                .actionAt(LocalDateTime.now())
                .restrictionEndAt(user.getWriteRestrictedUntil())
                .build();
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

    // 공통 작성자 추출 메서드
    private UUID getAuthorUuidByTarget(UUID mateUuid, Long mateReplyId) {
        if (mateUuid != null) {
            Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND));
            UserEntity user = userRepository.findById(mate.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            return user.getUserUuid();
        } else if (mateReplyId != null) {
            MateReply reply = mateReplyRepository.findByMateReplyIdAndDeletedAtIsNull(mateReplyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATE_REPLY_NOT_FOUND));
            UserEntity user = userRepository.findById(reply.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            return user.getUserUuid();
        } else {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_SPECIFIED);
        }
    }

    //신고된 Mate(게시글) 또는 MateReply(댓글)인지 확인
    private void validateReportedTarget(UUID mateUuid, Long mateReplyId) {
        if (mateUuid != null) {
            Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND));
            boolean isReported = mateReportRepository.existsByMateId(mate.getMateId());
            if (!isReported) {
                throw new BusinessException(ErrorCode.MATE_NOT_REPORTED);
            }
        } else if (mateReplyId != null) {
            MateReply reply = mateReplyRepository.findByMateReplyIdAndDeletedAtIsNull(mateReplyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATE_REPLY_NOT_FOUND));
            boolean isReported = mateReportRepository.existsByMateReplyId(mateReplyId);
            if (!isReported) {
                throw new BusinessException(ErrorCode.MATE_REPLY_NOT_REPORTED);
            }
        } else {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_SPECIFIED);
        }
    }

}

