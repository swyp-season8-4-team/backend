package org.swyp.dessertbee.community.mate.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Pageable;
import org.swyp.dessertbee.community.mate.dto.request.MateReplyCreateRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateReportRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateAppReplyPageResponse;
import org.swyp.dessertbee.community.mate.dto.response.MateReplyPageResponse;
import org.swyp.dessertbee.community.mate.dto.response.MateReplyResponse;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;

import java.util.List;
import java.util.UUID;

public interface MateReplyService {

    /** 디저트메이트 댓글 생성 */
    MateReplyResponse createReply(UUID mateUuid, MateReplyCreateRequest request, HttpServletRequest httpRequest);

    /** 디저트메이트 댓글 조회(한개만) */
    MateReplyResponse getReplyDetail(UUID mateUuid, Long replyId);

    /** 디저트메이트 댓글 전체 조회 */
    MateReplyPageResponse getReplies(UUID mateUuid, Pageable pageable);

    /** 디저트메이트 댓글 전체 조회(앱)*/
    MateAppReplyPageResponse getAppReplies(UUID mateUuid, Pageable pageable);

    /** 디저트메이트 댓글 수정 */
    void updateReply(UUID mateUuid, Long replyId, MateReplyCreateRequest request);

    /** 디저트메이트 댓글 삭제 */
    void deleteReply(UUID mateUuid, Long replyId);

    /** 디저트메이트 댓글 신고 */
    void reportMateReply(UUID mateUuid, Long replyId, MateReportRequest request);


    //     -------------- 관리자용 메이트 댓글 신고 관리 기능 ------------

    /**  신고된 Mate 댓글 조회*/
    List<MateReportResponse> getReportedMateReplies();

    /** 신고된 Mate 댓글 삭제*/
    void deleteReportedMateReply(Long mateReplyId);

}
