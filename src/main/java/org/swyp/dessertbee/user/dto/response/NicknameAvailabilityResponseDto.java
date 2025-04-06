package org.swyp.dessertbee.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "닉네임 사용 가능 여부 응답")
public class NicknameAvailabilityResponseDto {

    @Schema(description = "닉네임 사용 가능 여부", example = "true")
    private boolean available;
}