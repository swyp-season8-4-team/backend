package org.swyp.dessertbee.community.mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.community.mate.entity.MateReport;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateReportResponse {
    private Long mateReportId;
    private Long reportCategoryId;
    private Long mateReplyId;
    private Long mateId;
    private Long userId;
    private String comment;
    private LocalDateTime createdAt;

    public MateReportResponse(MateReport report) {
        this.mateReportId = report.getMateReportId();
        this.reportCategoryId = report.getReportCategoryId();
        this.mateId = report.getMateId();
        this.mateReplyId = report.getMateReplyId();
        this.userId = report.getUserId();
        this.comment = report.getComment();
        this.createdAt = report.getCreatedAt();
    }
}
