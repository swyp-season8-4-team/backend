package org.swyp.dessertbee.community.mate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateRejectRequest {

    @NotBlank(message = "디저트메이트 생성한 사람의 uuid를 입력해주세요.")
    @Schema(description = "디저트메이트 생성한 사람의 uuid", defaultValue = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID creatorUserUuid;

    @NotBlank(message = "거절할 사람을 선택해주세요.")
    @Schema(description = "거절할 사람 uuid", defaultValue = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    //디저트메이트 거절할 때
    private UUID rejectUserUuid;

}
