package org.swyp.dessertbee.community.review.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

public class ReviewException {

    /**
     * 커뮤니티 리뷰 없을 때 예외
     * */
    public static class ReviewNotFoundException extends BusinessException {

        public ReviewNotFoundException() {
            super(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND);
        }

        public ReviewNotFoundException(String message) {
            super(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND, message);
        }
    }
}
