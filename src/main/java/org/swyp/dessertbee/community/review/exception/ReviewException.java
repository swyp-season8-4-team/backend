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

    /**
     * 커뮤니티 이미지 idx 예외
     * */
    public static class ImageIndexNotFoundException extends BusinessException {
        public ImageIndexNotFoundException() {
            super(ErrorCode.IMAGE_INDEX_NOT_FOUND);
        }

        public ImageIndexNotFoundException(String message) {
            super(ErrorCode.IMAGE_INDEX_NOT_FOUND, message);
        }
    }

    /**
     * deleteImageIds 가 중복 일 때 예외
     * */
    public static class deleteImageDuplicationFoundException extends BusinessException {


        public deleteImageDuplicationFoundException() {super(ErrorCode.DUPLICATE_DELETE_IMAGE_IDS);}

        public deleteImageDuplicationFoundException(String message) {
            super(ErrorCode.DUPLICATE_DELETE_IMAGE_IDS, message);

        }
    }
}
