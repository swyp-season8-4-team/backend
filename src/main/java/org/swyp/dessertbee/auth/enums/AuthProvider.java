package org.swyp.dessertbee.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증 제공자 유형
 */
@Getter
@RequiredArgsConstructor
public enum AuthProvider {
    LOCAL("local"),
    KAKAO("kakao"),
    APPLE("apple"); // 애플 소셜 로그인 제공자 추가

    private final String providerName;

    /**
     * 제공자 이름으로 AuthProvider 조회
     * @param providerName 제공자 이름
     * @return 해당하는 AuthProvider 또는 null
     */
    public static AuthProvider fromString(String providerName) {
        for (AuthProvider provider : AuthProvider.values()) {
            if (provider.getProviderName().equalsIgnoreCase(providerName)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * 제공자 이름 확인
     * @param providerName 확인할 제공자 이름
     * @return 유효한 제공자 이름인지 여부
     */
    public static boolean isValidProvider(String providerName) {
        return fromString(providerName) != null;
    }
}