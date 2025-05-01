package org.swyp.dessertbee.auth.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

/**
 * 애플 OAuth 응답을 처리하는 구현체
 * 애플 ID 토큰에서 추출한 정보를 표준화된 형태로 변환
 */
@Slf4j
public class AppleResponse implements OAuth2Response {
    private final Map<String, Object> attributes;

    /**
     * 애플 ID 토큰 페이로드로부터 AppleResponse 객체 생성
     *
     * @param attributes 애플 ID 토큰 페이로드 (claims)
     */
    public AppleResponse(Map<String, Object> attributes) {
        this.attributes = attributes;

        // sub와 email 필드 검증
        if (attributes.get("sub") == null) {
            log.error("OAuth2 애플 응답에 sub(사용자 ID)가 없습니다.");
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_response", "Missing subject identifier", null)
            );
        }

        // 애플은 email_verified 속성이 true인 경우에만 email을 신뢰할 수 있음
        if (attributes.get("email") == null) {
            log.error("OAuth2 애플 응답에 email이 없습니다.");
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_response", "Missing email data", null)
            );
        }
    }

    @Override
    public String getProvider() {
        // 제공자 이름 반환
        return "apple";
    }

    @Override
    public String getProviderId() {
        // 애플에서는 'sub' claim이 사용자 고유 ID
        return attributes.get("sub").toString();
    }

    @Override
    public String getEmail() {
        // 사용자 이메일 반환
        return attributes.get("email").toString();
    }

    @Override
    public String getNickname() {
        // 이름 정보가 있는 경우 사용 (첫 로그인 시에만 제공됨)
        String firstName = (String) attributes.getOrDefault("firstName", "");
        String lastName = (String) attributes.getOrDefault("lastName", "");
        String fullName = (lastName + firstName).trim();

        // 이름 정보가 없는 경우 이메일에서 생성
        if (fullName.isEmpty()) {
            // 이메일 주소에서 @ 앞부분을 닉네임으로 사용
            String email = getEmail();
            return email != null ? email.split("@")[0] : "Apple User";
        }
        return fullName;
    }

    @Override
    public String getImageUrl() {
        // 애플은 프로필 이미지를 제공하지 않음
        return null;
    }
}