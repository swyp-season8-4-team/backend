package org.swyp.dessertbee.store.notice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.swyp.dessertbee.store.notice.entity.NoticeTag;

import java.time.LocalDateTime;

public record StoreNoticeResponse(
        @NotNull
        @Schema(description = "공지사항 식별자 (PK)", example = "1")
        Long noticeId,

        @NotNull
        @Schema(
                description = "공지 태그 (ALERT: 알림, EMERGENCY: 긴급, COMMON: 일반",
                allowableValues = {"ALERT", "EMERGENCY", "COMMON"}
        )
        NoticeTag tag,

        @NotBlank
        @Schema(description = "공지 제목", example = "이벤트 공지입니다.")
        String title,

        @NotBlank
        @Schema(description = "공지 내용", example = "금일 오후 3시에 이벤트가 진행됩니다.")
        String content,

        @Schema(description = "공지 등록 시간", example = "2025-04-03T14:30:00")
        @NotNull(message = "공지 등록 시간은 필수입니다.")
        LocalDateTime createdAt,

        @Schema(description = "공지 수정 시간", example = "2025-04-03T15:00:00")
        LocalDateTime updatedAt
) {}