package org.swyp.dessertbee.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AppleLoginRequest {

    /**
     * Apple에서 제공하는 일회용 인증 코드
     */
    private String code;

    /**
     * Apple에서 제공하는 ID 토큰 (JWT)
     */
    @JsonProperty("id_token")
    private String idToken;

    /**
     * CSRF 방지를 위한 상태값
     */
    private String state;

    /**
     * 사용자 정보 (최초 로그인 시에만 포함)
     */
    @JsonProperty("user")
    private AppleUserInfo userInfo;

    /**
     * Apple 사용자 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppleUserInfo {
        /**
         * 사용자 이메일
         */
        private String email;

        /**
         * 사용자 이름 정보
         */
        private Name name;

        /**
         * Apple 사용자 이름 구조
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Name {
            /**
             * 이름
             */
            @JsonProperty("firstName")
            private String firstName;

            /**
             * 성
             */
            @JsonProperty("lastName")
            private String lastName;
        }
    }
}