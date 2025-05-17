package org.swyp.dessertbee.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "차단 여부 확인 응답")
public class UserBlockCheckResponse {

    @Schema(description = "차단 여부", example = "true")
    private boolean isBlocked;

    @Schema(description = "차단 ID (차단된 경우에만 값이 있음)", example = "2")
    private Long id;

    @Schema(description = "차단한 사용자 UUID", example = "46743110-931e-404b-a2cb-02e5634e2423")
    private UUID blockerUserUuid;

    @Schema(description = "차단된 사용자 UUID", example = "4e80465b-c081-432b-a97f-f89edec5c1e3")
    private UUID blockedUserUuid;

    @Schema(description = "차단된 사용자 닉네임", example = "매력적인케이크4135")
    private String blockedUserNickname;

    @Schema(description = "차단 일시 (차단된 경우에만 값이 있음)", example = "2025-05-17T00:30:58.742684")
    private LocalDateTime createdAt;
}