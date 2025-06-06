package org.swyp.dessertbee.community.mate.service;

import org.springframework.data.domain.Pageable;
import org.swyp.dessertbee.community.mate.dto.request.MateAppReplyCreateRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateReplyCreateRequest;
import org.swyp.dessertbee.common.dto.ReportRequest;
import org.swyp.dessertbee.community.mate.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface MateReplyService {

    /** 디저트메이트 댓글 생성 */
    MateReplyResponse createReply(UUID mateUuid, MateReplyCreateRequest request);

    MateReplyResponse createAppReply(UUID mateUuid, MateAppReplyCreateRequest request);
    /** 디저트메이트 댓글 조회(한개만) */
    MateReplyResponse getReplyDetail(UUID mateUuid, Long replyId);

    /** 디저트메이트 댓글 조회(한개만)(앱) */
    MateAppReplyResponse getAppReplyDetail(UUID mateUuid, Long replyId);
    /** 디저트메이트 댓글 전체 조회 */
    MateReplyPageResponse getReplies(UUID mateUuid, Pageable pageable);

    /** 디저트메이트 댓글 전체 조회(앱)*/
    MateAppReplyPageResponse getAppReplies(UUID mateUuid, Pageable pageable);

    /** 디저트메이트 댓글 수정 */
    void updateReply(UUID mateUuid, Long replyId, MateReplyCreateRequest request);

    /** 디저트메이트 댓글 삭제 */
    void deleteReply(UUID mateUuid, Long replyId);

    /** 디저트메이트 댓글 신고 */
    void reportMateReply(UUID mateUuid, Long replyId, ReportRequest request);

}
