package org.swyp.dessertbee.store.store.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 가게 저장 관련 예외 클래스들을 모아둔 클래스
 */
public class UserStoreExceptions {

    /**
     * 가게 저장 리스트 예외
     */
    public static class StoreListNotFoundException extends BusinessException {
        public StoreListNotFoundException() {
            super(ErrorCode.STORE_LIST_NOT_FOUND);
        }

        public StoreListNotFoundException(String message) {
            super(ErrorCode.STORE_LIST_NOT_FOUND, message);
        }
    }

    /**
     * 리스트 이름 중복 예외
     */
    public static class DuplicateListNameException extends BusinessException {
        public DuplicateListNameException() {
            super(ErrorCode.STORE_DUPLICATE_LIST_NAME);
        }

        public DuplicateListNameException(String message) {
            super(ErrorCode.STORE_DUPLICATE_LIST_NAME, message);
        }
    }

    /**
     * 가게 저장 서비스 오류 예외
     */
    public static class UserStoreServiceException extends BusinessException {
        public UserStoreServiceException() {
            super(ErrorCode.USER_STORE_SERVICE_ERROR);
        }

        public UserStoreServiceException(String message) {
            super(ErrorCode.USER_STORE_SERVICE_ERROR, message);
        }
    }

    /**
     * 저장 리스트 생성 예외
     */
    public static class ListCreationFailedException extends BusinessException {
        public ListCreationFailedException() {
            super(ErrorCode.STORE_LIST_CREATION_FAILED);
        }

        public ListCreationFailedException(String message) {
            super(ErrorCode.STORE_LIST_CREATION_FAILED, message);
        }
    }

    /**
     * 저장 리스트 수정 예외
     */
    public static class ListUpdateFailedException extends BusinessException {
        public ListUpdateFailedException() {
            super(ErrorCode.STORE_LIST_UPDATE_FAILED);
        }

        public ListUpdateFailedException(String message) {
            super(ErrorCode.STORE_LIST_UPDATE_FAILED, message);
        }
    }

    /**
     * 저장 리스트 삭제 예외
     */
    public static class ListDeleteFailedException extends BusinessException {
        public ListDeleteFailedException() {
            super(ErrorCode.STORE_LIST_DELETE_FAILED);
        }

        public ListDeleteFailedException(String message) {
            super(ErrorCode.STORE_LIST_DELETE_FAILED, message);
        }
    }

    /**
     * 리스트에 이미 존재하는 가게 저장 예외
     */
    public static class DuplicateStoreSaveException extends BusinessException {
        public DuplicateStoreSaveException() {
            super(ErrorCode.DUPLICATE_STORE_SAVE);
        }

        public DuplicateStoreSaveException(String message) {
            super(ErrorCode.DUPLICATE_STORE_SAVE, message);
        }
    }

    /**
     * 가게 저장 오류 예외
     */
    public static class StoreSaveException extends BusinessException {
        public StoreSaveException() {
            super(ErrorCode.STORE_SAVE_FAILED);
        }

        public StoreSaveException(String message) {
            super(ErrorCode.STORE_SAVE_FAILED, message);
        }
    }

    /**
     * 가게 저장 취소 오류 예외
     */
    public static class SavedStoreDeleteException extends BusinessException {
        public SavedStoreDeleteException() {
            super(ErrorCode.SAVED_STORE_DELETE_FAILED);
        }

        public SavedStoreDeleteException(String message) {
            super(ErrorCode.SAVED_STORE_DELETE_FAILED, message);
        }
    }

    /**
     * 저장된 가게 예외
     */
    public static class SavedStoreNotFoundException extends BusinessException {
        public SavedStoreNotFoundException() {
            super(ErrorCode.SAVED_STORE_NOT_FOUND);
        }

        public SavedStoreNotFoundException(String message) {
            super(ErrorCode.SAVED_STORE_NOT_FOUND, message);
        }
    }
}
