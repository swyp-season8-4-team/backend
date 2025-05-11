package org.swyp.dessertbee.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    @Schema(
            description = "새로 발급된 JWT 액세스 토큰",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJkZXNzZXJ0YmVlLmNvbSIsImlhdCI6MTc0MzYwNDczNCwiZXhwIjoxNzQzODYzOTM0LCJqdGkiOiIyZTgwYzM2Ni1lNDUzLTQ5YTktYWIyOC0yZGM5NDA1ZmY1ZTYiLCJ0eXBlIjoiQUNDRVNTIiwic3ViIjoiOGQwNmVmN2EtNzEyNy00NWU0LTgwYzItYWI3ZWI1Yjc0NzU3Iiwicm9sZXMiOlsiUk9MRV9VU0VSIl19.fqCd-U3gTyUwUc2dDR74HgK45vxvq4-wcgIv6-hOpBc",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accessToken;

    @Schema(
            description = "토큰 타입 (항상 'Bearer')",
            example = "Bearer",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String tokenType;

    @Schema(
            description = "토큰 만료 시간 (밀리초 단위)",
            example = "259200000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long expiresIn;

    @Schema(
            description = "디바이스 식별자",
            example = "5fa946a8-3374-4df3-8800-f869eb070c07",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String deviceId;
}
