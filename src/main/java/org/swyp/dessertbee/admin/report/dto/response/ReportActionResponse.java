package org.swyp.dessertbee.admin.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReportActionResponse {

    private UUID mateUuid;              // 신고된 게시글 UUID (null 가능)
    private Long mateReplyId;           // 신고된 댓글 ID (null 가능)
    private UUID userUuid;              // 조치 대상 사용자 UUID

    private LocalDateTime actionAt;     // 조치 일시

    // 아래 두 필드는 조치 유형에 따라 한쪽만 값이 들어감
    private LocalDateTime restrictionEndAt; // 작성제한 만료일 (작성제한 시)
    private LocalDateTime suspendedUntil;   // 정지 만료일 (정지 시)
}
