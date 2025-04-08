package org.swyp.dessertbee.community.mate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    @Schema(description = "디저트메이트 신고 id", example = "1")
    private Long mateReportId;

    @NotBlank
    @Schema(description = "디저트메이트 신고 id", example = "1")
    private Long reportCategoryId;


    @Schema(description = "신고하는 디저트메이트 댓글 id", example = "7")
    private Long mateReplyId;

    @Schema(description = "신고하는 디저트메이트 id", example = "3")
    private Long mateId;

    @NotBlank
    @Schema(description = "신고하는 사람 id", example = "22")
    private Long userId;

    @NotBlank
    @Schema(description = "신고 내용", example = "욕설 및 폭언")
    private String comment;

    @NotBlank
    @Schema(description = "신고 날짜", example = "2025-03-10 02:15")
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
