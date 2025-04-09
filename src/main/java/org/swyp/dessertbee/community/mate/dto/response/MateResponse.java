package org.swyp.dessertbee.community.mate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateResponse {


    @NotBlank
    @Schema(description = "디저트메이트 uuid", example = "3037ab04-195e-48d1-83e2-e005899fc74d")
    private UUID mateUuid;

    @NotBlank
    @Schema(description = "디저트메이트 카테고리명", example = "빵지순례")
    private String mateCategory;

    @NotBlank
    @Schema(description = "디저트메이트 사진 썸네일")
    private String thumbnail;


    @NotBlank
    @Schema(description = "작성하는 사람 닉네임", defaultValue = "false", example = "디저비1")
    private String nickname;

    @NotBlank
    @Schema(description = "디저트메이트 제목 작성", defaultValue = "false", example = "저랑 같이 홍대 빵지순례할 사람 찾습니다.")
    private String title;

    @NotBlank
    @Schema(description = "디저트메이트 내용 작성", defaultValue = "false", example = "홍대 빵지순례 리스트 200개 있습니다. 같이 맛있는거 먹으러 가요.")
    private String content;

    @NotNull
    @Schema(description = "디저트메이트 모집 여부", defaultValue = "true", example = "true")
    private Boolean recruitYn;

    @NotNull
    @Schema(description = "현재 로그인한 사용자의 디저트메이트 저장 유무")
    private boolean saved;
}
