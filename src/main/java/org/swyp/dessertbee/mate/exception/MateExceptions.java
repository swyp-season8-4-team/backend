package org.swyp.dessertbee.mate.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

public class MateExceptions {


    /**
     * 디저트메이트 없을 때 예외
     * */
    public static class MateNotFoundException extends BusinessException {
        public MateNotFoundException(){super(ErrorCode.MATE_NOT_FOUND);}

        public  MateNotFoundException(String message) {
            super(ErrorCode.MATE_NOT_FOUND, message);
        }
    }

    public static class UserNotFoundExcption extends BusinessException {
        public UserNotFoundExcption(){super(ErrorCode.USER_NOT_FOUND);}

        public UserNotFoundExcption(String message) {
            super(ErrorCode.USER_NOT_FOUND,message );
        }
    }

    /**
     * 디저트메이트 신청 중복
     * */
    public static class DuplicateApplyException extends BusinessException {
        public DuplicateApplyException(){super(ErrorCode.DUPLICATE_APPLY);}

        public DuplicateApplyException(String message) {
            super(ErrorCode.USER_NOT_FOUND,message );
        }

    }

    /**
     * 디저트메이트 목록 조회 예외(잘못된 범위 설정)
     * */
    public static class FromToMateException extends BusinessException {
        public FromToMateException(){super(ErrorCode.INVALID_RANGE);}

        public FromToMateException(String message) {
            super(ErrorCode.INVALID_RANGE, message);
        }
    }
}
