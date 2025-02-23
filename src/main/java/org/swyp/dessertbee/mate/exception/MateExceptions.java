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

    /**
     * 유저 예외
     * */
    public static class UserNotFoundExcption extends BusinessException {
        public UserNotFoundExcption(){super(ErrorCode.USER_NOT_FOUND);}

        public UserNotFoundExcption(String message) {
            super(ErrorCode.USER_NOT_FOUND,message );
        }
    }

    /**
     * 디저트 메이트 멤버 예외
     * */
    public static class MateMemberNotFoundExcption extends BusinessException {
        public MateMemberNotFoundExcption(){super(ErrorCode.MATE_MEMBER_NOT_FOUND);}
        public MateMemberNotFoundExcption(String message) {super(ErrorCode.MATE_MEMBER_NOT_FOUND, message);}
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

    /**
     * 디저트메이트 신청 대기 예외
     * */
    public static class MateApplyWaitException extends BusinessException {
        public MateApplyWaitException(){super(ErrorCode.MATE_APPLY_WAIT);}

        public MateApplyWaitException(String message) {
            super(ErrorCode.MATE_APPLY_WAIT, message);
        }
    }

    /**
     * 디저트메이트 신청 강퇴 예외
     * */
    public static class MateApplyBannedException extends BusinessException {

        public MateApplyBannedException(){super(ErrorCode.MATE_APPLY_REJECT);}

        public MateApplyBannedException(String message) {
            super(ErrorCode.MATE_APPLY_BANNED, message);
        }
    }

    /**
     * 디저트메이트 신청 거절 예외
     * */
    public static class MateApplyRejectException extends BusinessException {
        public MateApplyRejectException(){super(ErrorCode.MATE_APPLY_REJECT);}
        public MateApplyRejectException(String message) {
            super(ErrorCode.MATE_APPLY_REJECT, message);
        }
    }

    /**
     * 디저트메이트 신청자가 기존 팀원일 때 예외
     * */
    public static class AlreadyTeamMemberException extends BusinessException {
        public AlreadyTeamMemberException() {
            super(ErrorCode.ALREADY_TEAM_MEMBER);
        }
        public AlreadyTeamMemberException(String message) {
            super(ErrorCode.ALREADY_TEAM_MEMBER, message);
        }
    }

    /**
     * 디저트 메이트 멤버 관리자 권한 예외
     * */
    public static class PermissionDeniedException extends BusinessException {
        public PermissionDeniedException(){
            super(ErrorCode.MATE_PERMISSION_DENIED);
        }
        public PermissionDeniedException(String message) {
            super(ErrorCode.MATE_PERMISSION_DENIED,message);
        }
    }


    /**
     *  디저트 메이트 댓글 예외
     * */
    public static class MateReplyNotFoundException extends BusinessException {
        public MateReplyNotFoundException(){super(ErrorCode.MATE_REPLY_NOT_FOUND);}

        public MateReplyNotFoundException(String message) {
            super(ErrorCode.MATE_REPLY_NOT_FOUND, message);
        }
    }
    /**
     * 디저트 메이트 사진 2개 이상 예외
     * */
    public static class MateImageCountExceededException extends BusinessException {

        public MateImageCountExceededException()
        {
            super(ErrorCode.MATE_IMAGE_COUNT_EXCEEDED);
        }

        public MateImageCountExceededException(String message){
            super(ErrorCode.MATE_IMAGE_COUNT_EXCEEDED, message);
        }

    }

    /**
     * 댓글 작성자가 아닐때 예외
     * */
    public static class NotCommentAuthorException extends BusinessException {
        public NotCommentAuthorException(){
            super(ErrorCode.MATE_REPLY_NOT_AUTHOR);
        }
        public NotCommentAuthorException(String message) {
            super(ErrorCode.MATE_REPLY_NOT_AUTHOR, message);
        }
    }
}
