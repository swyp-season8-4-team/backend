package org.swyp.dessertbee.store.review.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 가게 한줄리뷰 관련 예외 클래스들을 모아둔 클래스
 */
public class StoreReviewExceptions {

    /**
     * 가게 한줄리뷰 예외
     */
    public static class StoreReviewNotFoundException extends BusinessException {
        public StoreReviewNotFoundException() {
            super(ErrorCode.STORE_REVIEW_NOT_FOUND);
        }

        public StoreReviewNotFoundException(String message) {
            super(ErrorCode.STORE_REVIEW_NOT_FOUND, message);
        }
    }

    /**
     * 가게 한줄리뷰 등록 예외
     */
    public static class StoreReviewCreationFailedException extends BusinessException {
        public StoreReviewCreationFailedException() {
            super(ErrorCode.STORE_REVIEW_CREATION_FAILED);
        }

        public StoreReviewCreationFailedException(String message) {
            super(ErrorCode.STORE_REVIEW_CREATION_FAILED, message);
        }
    }

    /**
     * 가게 한줄리뷰 수정 예외
     */
    public static class StoreReviewUpdateFailedException extends BusinessException {
        public StoreReviewUpdateFailedException() {
            super(ErrorCode.STORE_REVIEW_UPDATE_FAILED);
        }

        public StoreReviewUpdateFailedException(String message) {
            super(ErrorCode.STORE_REVIEW_UPDATE_FAILED, message);
        }
    }

    /**
     * 가게 한줄리뷰 삭제 예외
     */
    public static class StoreReviewDeleteFailedException extends BusinessException {
        public StoreReviewDeleteFailedException() {
            super(ErrorCode.STORE_REVIEW_DELETE_FAILED);
        }

        public StoreReviewDeleteFailedException(String message) {
            super(ErrorCode.STORE_REVIEW_DELETE_FAILED, message);
        }
    }

    /**
     * 가게 한줄리뷰 오류 예외
     */
    public static class StoreReviewServiceException extends BusinessException {
        public StoreReviewServiceException() {
            super(ErrorCode.STORE_REVIEW_SERVICE_ERROR);
        }

        public StoreReviewServiceException(String message) {
            super(ErrorCode.STORE_REVIEW_SERVICE_ERROR, message);
        }
    }

    /**
     * 유효하지 않은 한줄리뷰 uuid 예외
     */
    public static class InvalidStoreReviewUuidException extends BusinessException {
        public InvalidStoreReviewUuidException() {
            super(ErrorCode.INVALID_STORE_REVIEW_UUID);
        }

        public InvalidStoreReviewUuidException(String message) {
            super(ErrorCode.INVALID_STORE_REVIEW_UUID, message);
        }
    }

    /**
     * 유효하지 않은 한줄리뷰 예외
     */
    public static class InvalidStoreReviewException extends BusinessException {
        public InvalidStoreReviewException() {
            super(ErrorCode.STORE_REVIEW_NOT_FOUND);
        }

        public InvalidStoreReviewException(String message) {
            super(ErrorCode.STORE_REVIEW_NOT_FOUND, message);
        }
    }

    /**
     * 유효하지 않은 한줄리뷰 예외
     */
    public static class StoreReviewAlreadyExistsTodayException extends BusinessException {
        public StoreReviewAlreadyExistsTodayException() {
            super(ErrorCode.STORE_REVIEW_ALREADY_EXISTS_TODAY);
        }

        public StoreReviewAlreadyExistsTodayException(String message) {
            super(ErrorCode.STORE_REVIEW_NOT_FOUND, message);
        }
    }

    /**
     *
     * */
    public static class DuplicationReportException extends BusinessException {
        public DuplicationReportException(){super(ErrorCode.STORE_DUPLICATION_REPORT);}

        public DuplicationReportException(String message) {
            super(ErrorCode.STORE_DUPLICATION_REPORT, message);
        }
    }
}
