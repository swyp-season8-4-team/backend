package org.swyp.dessertbee.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 사용하는 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 에러가 발생했습니다."),

    // Auth
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "A001", "이미 등록된 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "A002", "이미 사용중인 닉네임입니다."),
    INVALID_VERIFICATION_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 인증 토큰입니다."),
    EXPIRED_VERIFICATION_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "만료된 인증 토큰입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "A005", "비밀번호가 일치하지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A006", "잘못된 인증 정보입니다."),
    SIGNUP_RESTRICTED_DELETED_ACCOUNT(HttpStatus.FORBIDDEN, "A007", "탈퇴한 계정은 30일 이후에 재가입이 가능합니다."),

    // Email
    EMAIL_SENDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E005", "이메일 발송에 실패했습니다."),
    TOO_MANY_VERIFICATION_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "E001", "너무 많은 인증 요청이 있었습니다. 잠시 후 다시 시도해주세요."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "E006", "이미 가입된 이메일입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_DELETED(HttpStatus.GONE, "U002", "탈퇴한 사용자입니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "U003", "유효하지 않은 사용자 상태입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "U004", "해당 정보에 대한 접근 권한이 없습니다."),
    INVALID_USER_UUID(HttpStatus.BAD_REQUEST, "U005", "유효하지 않은 사용자 식별자입니다."),

    // 사장님 권한
    /**
     * 필요한 에러코드에 대하 추가적으로 더 적으시면 됩니다. - 영민 -
     */
    // File
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
    FILE_UPDATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "파일 업데이트에 실패했습니다."),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "파일 삭제에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F004", "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F005", "파일 크기가 제한을 초과했습니다."),


    MATE_NOT_FOUND(HttpStatus.NOT_FOUND,"M001" ,"존재하지 않는 디저트메이트입니다." ),
    MATES_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "해당 범위에서 메이트 데이터를 찾을 수 없습니다."),
    INVALID_RANGE(HttpStatus.BAD_REQUEST, "M003", "잘못된 범위 요청입니다."),
    MATE_APPLY_WAIT(HttpStatus.CONFLICT, "M004", "메이트 신청 대기 중입니다."),
    MATE_APPLY_BANNED(HttpStatus.FORBIDDEN, "M005" , "디저트메이트 강퇴 당한 사람입니다. 신청 불가능합니다."),
    MATE_APPLY_REJECT(HttpStatus.FORBIDDEN, "M006" , "거절 된 메이트입니다. 신청 불가능합니다."),
    ALREADY_TEAM_MEMBER(HttpStatus.CONFLICT,"M007" , "해당 사용자는 이미 팀원입니다."),
    MATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "M008","메이트 관리자 권한이 없습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}