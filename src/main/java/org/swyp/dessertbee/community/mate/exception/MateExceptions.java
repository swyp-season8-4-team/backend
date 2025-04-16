package org.swyp.dessertbee.community.mate.exception;

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
    public static class MatePendingException extends BusinessException {
        public MatePendingException(){super(ErrorCode.MATE_APPLY_WAIT);}

        public MatePendingException(String message) {
            super(ErrorCode.MATE_APPLY_WAIT, message);
        }
    }

    /**
     * 디저트메이트 신청 대기 아닌 사람 예외
     * */
    public static class MateNotPendingException extends BusinessException {
        public MateNotPendingException() {
            super(ErrorCode.MATE_NOT_PENDING_MEMBER);
        }

        public MateNotPendingException(String message) {
            super(ErrorCode.MATE_NOT_PENDING_MEMBER, message);
        }
    }

    /**
     * 디저트메이트 신청 강퇴 예외
     * */
    public static class MateApplyBannedException extends BusinessException {

        public MateApplyBannedException(){super(ErrorCode.MATE_APPLY_BANNED);}

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
     * 디저트메이트 저장 존재 여부
     * */
    public static class SavedMateNotFoundException extends BusinessException {
        public SavedMateNotFoundException(){super(ErrorCode.SAVED_MATE_NOT_FOUND);}

        public SavedMateNotFoundException(String message) {
            super(ErrorCode.SAVED_MATE_NOT_FOUND, message);
        }
    }

    /**
     * 디저트메이트 모집 마감 예외
     * */
    public static class MateRecruitDoneException extends BusinessException {
        public MateRecruitDoneException(){super(ErrorCode.MATE_RECRUIT_DONE);}

        public MateRecruitDoneException(String message) {
            super(ErrorCode.MATE_RECRUIT_DONE, message);
        }
    }

    /**
     * 디저트메이트 신고 예외
     * */
    public static class DuplicationReportException extends BusinessException {
        public DuplicationReportException(){super(ErrorCode.DUPLICATION_REPORT);}

        public DuplicationReportException(String message) {
            super(ErrorCode.DUPLICATION_REPORT, message);
        }
    }

    /**
     * 디저트메이트 중복 저장 예외
     * */
    public static class DuplicationSavedMateException extends BusinessException {
        public DuplicationSavedMateException()
        {
            super(ErrorCode.DUPLICATION_SAVED_MATE);
        }

        public DuplicationSavedMateException(String message) {
            super(ErrorCode.DUPLICATION_SAVED_MATE, message);
        }
    }

    /**
     * 디저트메이트 신고되지 않은 예외
     * */
    public static class MateReportNotFoundException extends BusinessException {
        public MateReportNotFoundException() {super(ErrorCode.MATE_NOT_REPORTED);}

        public MateReportNotFoundException(String message) {
            super(ErrorCode.MATE_NOT_REPORTED, message);
        }
    }

    /**
     * 디저트메이트 댓글 신고되지 않은 예외
     * */
    public static class MateReplyNotReportedException extends BusinessException {

        public MateReplyNotReportedException()
        {
            super(ErrorCode.MATE_REPLY_NOT_REPORTED);
        }

        public MateReplyNotReportedException(String message) {
            super(ErrorCode.MATE_REPLY_NOT_REPORTED, message);
        }
    }

    /**(HTTP code 500) server error - Ports are not available: exposing port TCP 0.0.0.0:6379 -> 127.0.0.1:0: listen tcp 0.0.0.0:6379: bind: address already in use
     * 디저트메이트 수용인원 제한 예외
     * */
    public static class MateCapacityExceededException extends BusinessException {

        public MateCapacityExceededException(){
            super(ErrorCode.MATE_CAPACITY_EXCEEDED);
        }

        public MateCapacityExceededException(String message) {
         super(ErrorCode.MATE_CAPACITY_EXCEEDED, message);
        }
    }

    /**
     * 디저트메이트 대댓글 제한 예외처리
     * */
    public static class InvalidReplyDepthException extends BusinessException {

        public InvalidReplyDepthException(){
            super(ErrorCode.INVALID_REPLY_DEPTH);
        }

        public InvalidReplyDepthException(String message) {
            super(ErrorCode.INVALID_REPLY_DEPTH, message);
        }

    }
}