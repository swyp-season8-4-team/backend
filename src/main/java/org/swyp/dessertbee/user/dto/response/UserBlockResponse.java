package org.swyp.dessertbee.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 사용자 차단 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 차단 응답")
public class UserBlockResponse {

    @Schema(description = "차단 ID", example = "1")
    private Long id;

    @Schema(description = "차단한 사용자 UUID", example = "e47ac10b-58cc-4372-a567-0e02b2c3d478")
    private UUID blockerUserUuid;

    @Schema(description = "차단된 사용자 UUID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID blockedUserUuid;

    @Schema(description = "차단된 사용자 닉네임", example = "디저트비")
    private String blockedUserNickname;

    @Schema(description = "차단 일시", example = "2023-06-15T14:30:00")
    private LocalDateTime createdAt;

    /**
     * 차단 목록 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "차단한 사용자 목록 응답")
    public static class ListResponse {

        @Schema(description = "차단한 사용자 목록")
        private List<UserBlockResponse> blockedUsers;

        @Schema(description = "총 차단 사용자 수", example = "3")
        private int totalCount;

    }
}