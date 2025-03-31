package org.swyp.dessertbee.admin.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;
import org.swyp.dessertbee.community.mate.service.MateReplyService;
import org.swyp.dessertbee.community.mate.service.MateService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MateReportAdminServiceImpl {

    private final MateService mateService;
    private final MateReplyService mateReplyService;

    // 신고된 Mate 게시글 목록 조회
    public List<MateReportResponse> getReportedMates() {
        return mateService.getReportedMates();
    }


    //Mate 게시글 삭제
    @Transactional
    public void deleteMateByUuid(UUID mateUuid) {
        mateService.deleteMateByUuid(mateUuid);
    }

    //신고된 Mate 댓글 조회
    public List<MateReportResponse> getReportedMateReplies() {
        return mateReplyService.getReportedMateReplies();
    }

    //Mate 댓글 삭제
    @Transactional
    public void deleteReportedMateReply(Long mateReplyId) {
        mateReplyService.deleteReportedMateReply(mateReplyId);
    }
}

