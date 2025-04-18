package org.swyp.dessertbee.search.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 가게 한줄리뷰 관련 예외 클래스들을 모아둔 클래스
 */
public class SearchExceptions {

    /**
     * 검색어 서비스 오류 예외
     */
    public static class SearchServiceException extends BusinessException {
        public SearchServiceException() {
            super(ErrorCode.SEARCH_SERVICE_ERROR);
        }

        public SearchServiceException(String message) {
            super(ErrorCode.SEARCH_SERVICE_ERROR, message);
        }
    }

    /**
     * 검색어 예외
     */
    public static class KeywordNotFoundException extends BusinessException {
        public KeywordNotFoundException() {
            super(ErrorCode.SEARCH_KEYWORD_NOT_FOUND);
        }

        public KeywordNotFoundException(String message) {
            super(ErrorCode.SEARCH_KEYWORD_NOT_FOUND, message);
        }
    }

    /**
     * 최근 검색어 저장 예외
     */
    public static class RecentCreationFailedException extends BusinessException {
        public RecentCreationFailedException() {
            super(ErrorCode.RECENT_KEYWORD_CREATION_FAILED);
        }

        public RecentCreationFailedException(String message) {
            super(ErrorCode.RECENT_KEYWORD_CREATION_FAILED, message);
        }
    }

    /**
     * 최근 검색어 삭제 예외
     */
    public static class RecentDeleteFailedException extends BusinessException {
        public RecentDeleteFailedException() {
            super(ErrorCode.RECENT_KEYWORD_DELETE_FAILED);
        }

        public RecentDeleteFailedException(String message) {
            super(ErrorCode.RECENT_KEYWORD_DELETE_FAILED, message);
        }
    }

    /**
     * 오래된 검색어 삭제 예외
     */
    public static class OldKeywordDeleteFailedException extends BusinessException {
        public OldKeywordDeleteFailedException() {
            super(ErrorCode.OLD_KEYWORD_DELETE_FAILED);
        }

        public OldKeywordDeleteFailedException(String message) {
            super(ErrorCode.OLD_KEYWORD_DELETE_FAILED, message);
        }
    }

    /**
     * 인기 검색어 저장(Redis) 예외
     */
    public static class PopularCreationFailedException extends BusinessException {
        public PopularCreationFailedException() {
            super(ErrorCode.POPULAR_KEYWORD_CREATION_FAILED);
        }

        public PopularCreationFailedException(String message) {
            super(ErrorCode.POPULAR_KEYWORD_CREATION_FAILED, message);
        }
    }

    /**
     * 인기 검색어 동기화 예외
     */
    public static class PopularSyncFailedException extends BusinessException {
        public PopularSyncFailedException() {
            super(ErrorCode.POPULAR_KEYWORD_SYNC_FAILED);
        }

        public PopularSyncFailedException(String message) {
            super(ErrorCode.POPULAR_KEYWORD_SYNC_FAILED, message);
        }
    }

    /**
     * 인기 검색어 초기화 예외
     */
    public static class PopularInitFailedException extends BusinessException {
        public PopularInitFailedException() {
            super(ErrorCode.POPULAR_KEYWORD_INIT_FAILED);
        }

        public PopularInitFailedException(String message) {
            super(ErrorCode.POPULAR_KEYWORD_INIT_FAILED, message);
        }
    }

    /**
     * 인기 검색어 초기화 예외
     */
    public static class InvalidPlatformException extends BusinessException {
        public InvalidPlatformException() {
            super(ErrorCode.INVALID_PLATFORM_VALUE);
        }

        public InvalidPlatformException(String message) {
            super(ErrorCode.INVALID_PLATFORM_VALUE, message);
        }
    }
}
