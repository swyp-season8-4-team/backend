package org.swyp.dessertbee.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 이미지를 포함한 회원가입 요청을 위한 DTO 클래스
 * SignUpRequest를 상속받아 프로필 이미지 필드만 추가
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SignUpWithProfileRequest extends SignUpRequest {

    @Schema(
            description = "프로필 이미지 (JPG, JPEG, PNG, GIF 형식의 5MB 이하 파일만 허용)",
            type = "string",
            format = "binary",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private MultipartFile profileImage;  // 프로필 이미지
}