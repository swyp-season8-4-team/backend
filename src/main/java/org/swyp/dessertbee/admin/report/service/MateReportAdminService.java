package org.swyp.dessertbee.admin.report.service;

import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.admin.report.dto.response.MateReplyReportCountResponse;
import org.swyp.dessertbee.admin.report.dto.response.MateReportCountResponse;
import org.swyp.dessertbee.admin.report.dto.response.ReportActionResponse;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MateReportAdminService {

    // 신고된 Mate 게시글 목록 조회
    List<MateReportResponse> getReportedMates();

    //Mate 신고 횟수 조회
    MateReportCountResponse getMateReportCount(UUID mateUuid);

    //Mate 게시글 삭제
    void deleteMateByUuid(UUID mateUuid);

    //신고된 Mate 댓글 조회
    List<MateReportResponse> getReportedMateReplies();

    //Mate 댓글 신고 횟수 조회
    MateReplyReportCountResponse getMateReplyReportCount(Long mateReplyId);

    //Mate 댓글 삭제
    void deleteReportedMateReply(Long mateReplyId);

    //사용자 경고
    ReportActionResponse warnAuthor(UUID mateUuid, Long mateReplyId, Long reportCategoryId);

    //사용자 정지
    ReportActionResponse suspendAuthor(UUID mateUuid, Long mateReplyId);

    //사용자 작성 제한
    ReportActionResponse restrictAuthorWriting(UUID mateUuid, Long mateReplyId);


}
