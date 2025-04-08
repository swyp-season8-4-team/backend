package org.swyp.dessertbee.store.notice.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 가게 공지 관련 예외 클래스들을 모아둔 클래스
 */
public class StoreNoticeExceptions {
    /**
     * 가게 공지 예외
     */
    public static class StoreNoticeNotFoundException extends BusinessException {
        public StoreNoticeNotFoundException() {
            super(ErrorCode.STORE_NOTICE_NOT_FOUND);
        }

        public StoreNoticeNotFoundException(String message) {
            super(ErrorCode.STORE_NOTICE_NOT_FOUND, message);
        }
    }

    /**
     * 가게 공지 등록 예외
     */
    public static class StoreNoticeCreationFailedException extends BusinessException {
        public StoreNoticeCreationFailedException() {
            super(ErrorCode.STORE_NOTICE_CREATION_FAILED);
        }

        public StoreNoticeCreationFailedException(String message) {
            super(ErrorCode.STORE_NOTICE_CREATION_FAILED, message);
        }
    }

    /**
     * 가게 공지 수정 예외
     */
    public static class StoreNoticeUpdateFailedException extends BusinessException {
        public StoreNoticeUpdateFailedException() {
            super(ErrorCode.STORE_NOTICE_UPDATE_FAILED);
        }

        public StoreNoticeUpdateFailedException(String message) {
            super(ErrorCode.STORE_NOTICE_UPDATE_FAILED, message);
        }
    }

    /**
     * 가게 공지 삭제 예외
     */
    public static class StoreNoticeDeleteFailedException extends BusinessException {
        public StoreNoticeDeleteFailedException() {
            super(ErrorCode.STORE_NOTICE_DELETE_FAILED);
        }

        public StoreNoticeDeleteFailedException(String message) {
            super(ErrorCode.STORE_NOTICE_DELETE_FAILED, message);
        }
    }

    /**
     * 가게 공지 오류 예외
     */
    public static class StoreNoticeServiceException extends BusinessException {
        public StoreNoticeServiceException() {
            super(ErrorCode.STORE_NOTICE_SERVICE_ERROR);
        }

        public StoreNoticeServiceException(String message) {
            super(ErrorCode.STORE_NOTICE_SERVICE_ERROR, message);
        }
    }

    /**
     * 유효하지 않은 공지 예외
     */
    public static class InvalidStoreNoticeException extends BusinessException {
        public InvalidStoreNoticeException() {
            super(ErrorCode.STORE_NOTICE_NOT_FOUND);
        }

        public InvalidStoreNoticeException(String message) {
            super(ErrorCode.STORE_NOTICE_NOT_FOUND, message);
        }
    }
}
