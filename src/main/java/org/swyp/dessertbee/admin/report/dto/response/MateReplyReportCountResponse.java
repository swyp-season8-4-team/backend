package org.swyp.dessertbee.admin.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class MateReplyReportCountResponse {
    private Long mateReplyId;
    private Long totalReportCount;
    private Map<Long, Long> categoryReportCounts; // <카테고리ID, 신고수>
}
