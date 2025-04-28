package org.swyp.dessertbee.admin.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateReportAdminServiceImpl implements MateReportAdminService {

    private final MateRepository mateRepository;
    private final MateReportRepository mateReportRepository;
    private final MateReplyRepository mateReplyRepository;

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
     * 신고된 Mate 게시글 삭제
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
     *  신고된 Mate 댓글 조회
     */
    public List<MateReportResponse> getReportedMateReplies() {
        List<MateReport> reports = mateReportRepository.findAllByMateReplyIdIsNotNull();

        return reports.stream()
                .map(MateReportResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 신고된 Mate 댓글 삭제
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
}

