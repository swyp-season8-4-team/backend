package org.swyp.dessertbee.preference.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 취향 관련 예외 클래스들을 모아둔 클래스
 */
public class PreferenceExceptions {

    /**
     * 취향 태그 조회 오류 예외
     */
    public static class PreferencesNotFoundException extends BusinessException {
        public PreferencesNotFoundException() {
            super(ErrorCode.PREFERENCES_NOT_FOUND);
        }

        public PreferencesNotFoundException(String message) {
            super(ErrorCode.PREFERENCES_NOT_FOUND, message);
        }
    }

    /**
     * 유저 취향 오류 예외
     */
    public static class UserPreferencesNotFoundException extends BusinessException {
        public UserPreferencesNotFoundException() {
            super(ErrorCode.USER_PREFERENCES_NOT_FOUND);
        }

        public UserPreferencesNotFoundException(String message) {
            super(ErrorCode.USER_PREFERENCES_NOT_FOUND, message);
        }
    }
}
