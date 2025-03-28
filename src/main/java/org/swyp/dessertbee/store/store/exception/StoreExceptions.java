package org.swyp.dessertbee.store.store.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 가게 관련 예외 클래스들을 모아둔 클래스
 */
public class StoreExceptions {

    /**
     * 가게 등록 예외
     */
    public static class StoreCreationFailedException extends BusinessException {
        public StoreCreationFailedException() {
            super(ErrorCode.STORE_CREATION_FAILED);
        }

        public StoreCreationFailedException(String message) {
            super(ErrorCode.STORE_CREATION_FAILED, message);
        }
    }

    /**
     * 가게 서비스 오류 예외
     */
    public static class StoreServiceException extends BusinessException {
        public StoreServiceException() {
            super(ErrorCode.STORE_SERVICE_ERROR);
        }

        public StoreServiceException(String message) {
            super(ErrorCode.STORE_SERVICE_ERROR, message);
        }
    }

    /**
     * 태그 저장 예외
     */
    public static class StoreTagSaveFailedException extends BusinessException {
        public StoreTagSaveFailedException() {
            super(ErrorCode.STORE_TAG_SAVE_FAILED);
        }

        public StoreTagSaveFailedException(String message) {
            super(ErrorCode.STORE_TAG_SAVE_FAILED, message);
        }
    }

    /**
     * 태그 선택 오류 예외 1
     */
    public static class InvalidTagSelectionException extends BusinessException {
        public InvalidTagSelectionException() {
            super(ErrorCode.INVALID_TAG_SELECTION);
        }

        public InvalidTagSelectionException(String message) {
            super(ErrorCode.INVALID_TAG_SELECTION, message);
        }
    }

    /**
     * 태그 선택 오류 예외 2
     */
    public static class InvalidTagIncludedException extends BusinessException {
        public InvalidTagIncludedException() {
            super(ErrorCode.INVALID_TAG_INCLUDED);
        }

        public InvalidTagIncludedException(String message) {
            super(ErrorCode.INVALID_TAG_INCLUDED, message);
        }
    }

    /**
     * 반경 내 가게 조회 오류 예외
     */
    public static class StoreMapReadException extends BusinessException {
        public StoreMapReadException() {
            super(ErrorCode.STORE_MAP_READ_FAILED);
        }

        public StoreMapReadException(String message) {
            super(ErrorCode.STORE_MAP_READ_FAILED, message);
        }
    }

    /**
     * 반경 내 가게 검색 오류 예외
     */
    public static class StoreSearchFailedException extends BusinessException {
        public StoreSearchFailedException() {
            super(ErrorCode.STORE_SEARCH_FAILED);
        }

        public StoreSearchFailedException(String message) {
            super(ErrorCode.STORE_SEARCH_FAILED, message);
        }
    }

    /**
     * 반경 내 사용자 취향 태그 맞춤 가게 조회 오류 예외
     */
    public static class PreferenceStoreReadException extends BusinessException {
        public PreferenceStoreReadException() {
            super(ErrorCode.PREFERENCE_STORE_READ_FAILED);
        }

        public PreferenceStoreReadException(String message) {
            super(ErrorCode.PREFERENCE_STORE_READ_FAILED, message);
        }
    }

    /**
     * 존재하지 않는 가게 예외
     */
    public static class StoreNotFoundException extends BusinessException {
        public StoreNotFoundException() {
            super(ErrorCode.STORE_NOT_FOUND);
        }

        public StoreNotFoundException(String message) {
            super(ErrorCode.STORE_NOT_FOUND, message);
        }
    }

    /**
     * 가게 정보 조회 예외
     */
    public static class StoreInfoReadFailedException extends BusinessException {
        public StoreInfoReadFailedException() {
            super(ErrorCode.STORE_INFO_READ_FAILED);
        }

        public StoreInfoReadFailedException(String message) {
            super(ErrorCode.STORE_INFO_READ_FAILED, message);
        }
    }

    /**
     * 가게 평점 업데이트 예외
     */
    public static class StoreRateUpdateException extends BusinessException {
        public StoreRateUpdateException() {
            super(ErrorCode.STORE_RATE_UPDATE_FAILED);
        }

        public StoreRateUpdateException(String message) {
            super(ErrorCode.STORE_RATE_UPDATE_FAILED, message);
        }
    }

    /**
     * 가게 수정 예외
     */
    public static class StoreUpdateException extends BusinessException {
        public StoreUpdateException() {
            super(ErrorCode.STORE_UPDATE_FAILED);
        }

        public StoreUpdateException(String message) {
            super(ErrorCode.STORE_UPDATE_FAILED, message);
        }
    }

    /**
     * 가게 삭제 예외
     */
    public static class StoreDeleteException extends BusinessException {
        public StoreDeleteException() {
            super(ErrorCode.STORE_DELETE_FAILED);
        }

        public StoreDeleteException(String message) {
            super(ErrorCode.STORE_DELETE_FAILED, message);
        }
    }

    /**
     * 유효하지 않은 가게 uuid 예외
     */
    public static class InvalidStoreUuidException extends BusinessException {
        public InvalidStoreUuidException() {
            super(ErrorCode.INVALID_STORE_UUID);
        }

        public InvalidStoreUuidException(String message) {
            super(ErrorCode.INVALID_STORE_UUID, message);
        }
    }
}
