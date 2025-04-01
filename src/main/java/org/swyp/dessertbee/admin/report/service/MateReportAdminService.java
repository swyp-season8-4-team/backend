package org.swyp.dessertbee.admin.report.service;

import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;

import java.util.List;
import java.util.UUID;

public interface MateReportAdminService {

    // 신고된 Mate 게시글 목록 조회
    List<MateReportResponse> getReportedMates();

    //Mate 게시글 삭제
    void deleteMateByUuid(UUID mateUuid);

    //신고된 Mate 댓글 조회
    List<MateReportResponse> getReportedMateReplies();

    //Mate 댓글 삭제
    void deleteReportedMateReply(Long mateReplyId);
}
