package org.swyp.dessertbee.store.menu.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 가게 메뉴 관련 예외 클래스들을 모아둔 클래스
 */
public class MenuExceptions {

    /**
     * 메뉴 서비스 오류 예외
     */
    public static class MenuServiceException extends BusinessException {
        public MenuServiceException() {
            super(ErrorCode.MENU_SERVICE_ERROR);
        }

        public MenuServiceException(String message) {
            super(ErrorCode.MENU_SERVICE_ERROR, message);
        }
    }

    /**
     * 메뉴 오류 예외
     */
    public static class MenuNotFoundException extends BusinessException {
        public MenuNotFoundException() {
            super(ErrorCode.STORE_MENU_NOT_FOUND);
        }

        public MenuNotFoundException(String message) {
            super(ErrorCode.STORE_MENU_NOT_FOUND, message);
        }
    }

    /**
     * 유효하지 않은 메뉴 uuid 예외
     */
    public static class InvalidMenuUuidException extends BusinessException {
        public InvalidMenuUuidException() {
            super(ErrorCode.INVALID_STORE_MENU_UUID);
        }

        public InvalidMenuUuidException(String message) {
            super(ErrorCode.INVALID_STORE_MENU_UUID, message);
        }
    }

    /**
     * 유효하지 않은 메뉴 예외
     */
    public static class InvalidMenuException extends BusinessException {
        public InvalidMenuException() {
            super(ErrorCode.INVALID_STORE_MENU);
        }

        public InvalidMenuException(String message) {
            super(ErrorCode.INVALID_STORE_MENU, message);
        }
    }

    /**
     * 단일 메뉴 수정 예외
     */
    public static class MenuUpdateFailedException extends BusinessException {
        public MenuUpdateFailedException() {
            super(ErrorCode.MENU_UPDATE_FAILED);
        }

        public MenuUpdateFailedException(String message) {
            super(ErrorCode.MENU_UPDATE_FAILED, message);
        }
    }

    /**
     * 단일 메뉴 삭제 예외
     */
    public static class MenuDeleteFailedException extends BusinessException {
        public MenuDeleteFailedException() {
            super(ErrorCode.MENU_DELETE_FAILED);
        }

        public MenuDeleteFailedException(String message) {
            super(ErrorCode.MENU_DELETE_FAILED, message);
        }
    }

    /**
     * 가게 메뉴 등록 예외
     */
    public static class MenuCreationFailedException extends BusinessException {
        public MenuCreationFailedException() {
            super(ErrorCode.MENU_CREATION_FAILED);
        }

        public MenuCreationFailedException(String message) {
            super(ErrorCode.MENU_CREATION_FAILED, message);
        }
    }
}
