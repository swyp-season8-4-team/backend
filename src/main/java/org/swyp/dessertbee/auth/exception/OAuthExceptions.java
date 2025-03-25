package org.swyp.dessertbee.auth.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

public class OAuthExceptions {

    public static class InvalidProviderException extends BusinessException {
        public InvalidProviderException() {
            super(ErrorCode.INVALID_PROVIDER);
        }

        public InvalidProviderException(String message) {
            super(ErrorCode.INVALID_PROVIDER, message);
        }
    }

    public static class OAuthAuthenticationException extends BusinessException {
        public OAuthAuthenticationException() {
            super(ErrorCode.AUTHENTICATION_FAILED);
        }

        public OAuthAuthenticationException(String message) {
            super(ErrorCode.AUTHENTICATION_FAILED, message);
        }
    }

}
