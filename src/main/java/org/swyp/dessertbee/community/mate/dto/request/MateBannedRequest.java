package org.swyp.dessertbee.community.mate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateBannedRequest {

    @NotBlank(message = "디저트메이트 생성한 사람의 uuid를 입력해주세요.")
    @Schema(description = "디저트메이트 생성한 사람의 uuid", defaultValue = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID creatorUserUuid;

    //디저트메이트 수락할 때
    @NotBlank(message = "디저트메이트 멤버 중 강퇴할 사람의 uuid를 입력해주세요.")
    @Schema(description = "디저트메이트 강퇴될 사람의 uuid", defaultValue = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID banUserUuid;


}
