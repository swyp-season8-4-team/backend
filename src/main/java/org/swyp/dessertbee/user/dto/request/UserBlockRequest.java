package org.swyp.dessertbee.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBlockRequest {
    @NotNull
    @Schema(description = "차단할 사용자의 UUID",
            example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID blockedUserUuid;
}