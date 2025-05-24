package org.swyp.dessertbee.statistics.common.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 가게 로그 저장 관련 예외 클래스들을 모아둔 클래스
 */
public class StoreStatisticsLogExceptions {

    /**
     * 디저트 메이트 생성/삭제
     */
    public static class MateLogCreateFailedException extends BusinessException {
        public MateLogCreateFailedException() {
            super(ErrorCode.MATE_LOG_CREATION_FAILED);
        }

        public MateLogCreateFailedException(String message) {
            super(ErrorCode.MATE_LOG_CREATION_FAILED, message);
        }
    }

    /**
     * 쿠폰 사용
     */
    public static class CouponUseLogCreateFailedException extends BusinessException {
        public CouponUseLogCreateFailedException() {
            super(ErrorCode.COUPON_USE_LOG_CREATION_FAILED);
        }

        public CouponUseLogCreateFailedException(String message) {
            super(ErrorCode.COUPON_USE_LOG_CREATION_FAILED, message);
        }
    }

    /**
     * 가게 한줄리뷰 등록/삭제
     */
    public static class StoreReviewLogCreateFailedException extends BusinessException {
        public StoreReviewLogCreateFailedException() {
            super(ErrorCode.STORE_REVIEW_LOG_CREATION_FAILED);
        }

        public StoreReviewLogCreateFailedException(String message) {
            super(ErrorCode.STORE_REVIEW_LOG_CREATION_FAILED, message);
        }
    }

    /**
     * 커뮤니티 리뷰 등록/삭제
     */
    public static class CommunityReviewLogCreateFailedException extends BusinessException {
        public CommunityReviewLogCreateFailedException() {
            super(ErrorCode.COMMUNITY_REVIEW_LOG_CREATION_FAILED);
        }

        public CommunityReviewLogCreateFailedException(String message) {
            super(ErrorCode.COMMUNITY_REVIEW_LOG_CREATION_FAILED, message);
        }
    }

    /**
     * 리스트에 가게 저장/취소
     */
    public static class StoreSaveLogCreateFailedException extends BusinessException {
        public StoreSaveLogCreateFailedException() {
            super(ErrorCode.STORE_SAVE_LOG_CREATION_FAILED);
        }

        public StoreSaveLogCreateFailedException(String message) {
            super(ErrorCode.STORE_SAVE_LOG_CREATION_FAILED, message);
        }
    }

    /**
     * 가게 상세조회
     */
    public static class StoreViewLogFailedException extends BusinessException {
        public StoreViewLogFailedException() {
            super(ErrorCode.STORE_VIEW_LOG_CREATION_FAILED);
        }

        public StoreViewLogFailedException(String message) {
            super(ErrorCode.STORE_VIEW_LOG_CREATION_FAILED, message);
        }
    }

    /**
     * 사용자 지정 기간별 통계 요청 오류
     */
    public static class CustomPeriodStatisticsException extends BusinessException {
        public CustomPeriodStatisticsException() {
            super(ErrorCode.CUSTOM_PERIOD_STATISTICS_FAILED);
        }

        public CustomPeriodStatisticsException(String message) {
            super(ErrorCode.CUSTOM_PERIOD_STATISTICS_FAILED, message);
        }
    }
}
