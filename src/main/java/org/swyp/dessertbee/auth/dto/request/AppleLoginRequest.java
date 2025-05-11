package org.swyp.dessertbee.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Apple 로그인 요청 데이터 전송 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Apple 로그인 요청 DTO")
public class AppleLoginRequest {

    /**
     * Apple에서 제공하는 일회용 인증 코드
     */
    @NotBlank
    @Schema(description = "Apple 인가 코드", example = "abcdef123456")
    private String code;

    /**
     * Apple에서 제공하는 ID 토큰 (JWT)
     */
    @NotBlank
    @JsonProperty("id_token")
    @Schema(description = "Apple ID 토큰 (JWT)", example = "eyJhbGciOi...")
    private String idToken;

    /**
     * CSRF 방지를 위한 상태값
     */
    @NotBlank
    @Schema(description = "CSRF 방지를 위한 상태값", example = "xyz789")
    private String state;

    /**
     * 사용자 정보 (최초 로그인 시에만 포함)
     */
    @Valid
    @JsonProperty("user")
    @Schema(description = "Apple 최초 로그인 시 제공되는 사용자 정보")
    private AppleUserInfo userInfo;

    /**
     * Apple 사용자 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Apple 사용자 정보")
    public static class AppleUserInfo {
        /**
         * 사용자 이메일
         */
        @Schema(description = "사용자 이메일", example = "user@example.com")
        private String email;

        /**
         * 사용자 이름 정보
         */
        @Valid
        @Schema(description = "Apple 사용자 이름 정보")
        private Name name;

        /**
         * Apple 사용자 이름 구조
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "Apple 사용자 이름 구조")
        public static class Name {
            /**
             * 이름
             */
            @JsonProperty("firstName")
            @Schema(description = "이름", example = "John")
            private String firstName;

            /**
             * 성
             */
            @JsonProperty("lastName")
            @Schema(description = "성", example = "Appleseed")
            private String lastName;
        }
    }
}
