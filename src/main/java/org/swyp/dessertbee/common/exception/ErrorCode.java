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
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A007", "인증에 실패했습니다."),
    SIGNUP_RESTRICTED_DELETED_ACCOUNT(HttpStatus.FORBIDDEN, "A008", "탈퇴한 계정은 30일 이후에 재가입이 가능합니다."),
    ROLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "A009", "해당 권한으로는 접근이 불가합니다."),
    AUTH_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A010", "인증 서비스 처리 중 오류가 발생했습니다."),
    OAUTH_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A011", "소셜 인증 서비스 처리 중 오류가 발생했습니다."),
    DEVICE_ID_MISSING(HttpStatus.BAD_REQUEST, "A012", "디바이스 ID가 제공되지 않았습니다."),
    INVALID_EMAIL(HttpStatus.UNAUTHORIZED, "A013", "이메일을 다시 확인해주세요."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A014", "비밀번호를 다시 입력해주세요."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "A015", "계정이 잠겼습니다."),

    // OAuth
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "O001", "지원되지 않는 OAuth 제공자입니다."),


    // JWT
    JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "J001", "유효하지 않은 JWT 서명입니다."),
    JWT_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "J002", "만료된 JWT 토큰입니다."),
    JWT_TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "J003", "지원되지 않는 JWT 토큰 형식입니다."),
    JWT_TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "J004", "잘못된 형식의 JWT 토큰입니다."),
    JWT_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "J005", "JWT 토큰이 제공되지 않았습니다."),

    // Email
    EMAIL_SENDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E005", "이메일 발송에 실패했습니다."),
    TOO_MANY_VERIFICATION_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "E001", "너무 많은 인증 요청이 있었습니다. 잠시 후 다시 시도해주세요."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "U003", "유효하지 않은 사용자 상태입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "U004", "해당 정보에 대한 접근 권한이 없습니다."),
    INVALID_USER_UUID(HttpStatus.BAD_REQUEST, "U005", "유효하지 않은 사용자 식별자입니다."),

    // Preference
    PREFERENCES_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "존재하지 않는 취향 태그입니다."),
    USER_PREFERENCES_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "취향을 등록하지 않은 사용자입니다."),

    // Search Keyword
    SEARCH_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "K001", "해당 검색 기록을 찾을 수 없습니다."),
    RECENT_KEYWORD_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K002", "최근 검색 기록 삭제에 실패했습니다."),
    SEARCH_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "K003", "검색어 서비스 처리 중 오류가 발생했습니다."),
    RECENT_KEYWORD_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K004", "최근 검색 기록 생성에 실패했습니다."),
    OLD_KEYWORD_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K005", "오래된 검색 기록 삭제에 실패했습니다."),
    POPULAR_KEYWORD_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K006", "인기 검색 기록 생성에 실패했습니다."),
    POPULAR_KEYWORD_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K007", "인기 검색 기록 동기화에 실패했습니다."),
    POPULAR_KEYWORD_INIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "K008", "인기 검색 기록 초기화에 실패했습니다."),

    // Log
    MATE_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L001", "디저트 메이트 로그 저장에 실패했습니다."),
    COUPON_USE_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L002", "쿠폰 사용 후 로그 저장에 실패했습니다."),
    STORE_REVIEW_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L003", "가게 한줄리뷰 로그 저장에 실패했습니다."),
    COMMUNITY_REVIEW_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L004", "커뮤니티 리뷰 로그 저장에 실패했습니다."),
    STORE_SAVE_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L005", "가게 저장 관련 로그 저장에 실패했습니다."),
    STORE_VIEW_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L006", "가게 조회 후 로그 저장에 실패했습니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 가게입니다."),
    STORE_CREATION_FAILED(HttpStatus.BAD_REQUEST, "S002", "가게 등록에 실패했습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S003", "가게에 대한 접근 권한이 없습니다."),
    STORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "S004", "이미 존재하는 가게입니다."),
    STORE_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "S005", "저장 리스트를 찾을 수 없습니다."),
    STORE_DUPLICATE_LIST_NAME(HttpStatus.CONFLICT, "S007", "동일한 이름의 리스트가 이미 존재합니다."),
    DUPLICATE_STORE_SAVE(HttpStatus.CONFLICT, "S009", "해당 가게는 이미 리스트에 존재합니다."),
    SAVED_STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S010", "해당 리스트에 저장된 가게가 없습니다."),
    INVALID_STORE_UUID(HttpStatus.BAD_REQUEST, "S011", "유효하지 않은 가게 식별자입니다."),
    INVALID_TAG_SELECTION(HttpStatus.BAD_REQUEST, "S012", "태그는 1개 이상 3개 이하로 선택해야 합니다."),
    INVALID_TAG_INCLUDED(HttpStatus.BAD_REQUEST, "S013", "유효하지 않은 태그가 포함되어 있습니다."),
    INVALID_STORE_REVIEW_UUID(HttpStatus.BAD_REQUEST, "S014", "유효하지 않은 한줄 리뷰 식별자입니다."),
    STORE_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "S015", "존재하지 않는 한줄 리뷰입니다."),
    INVALID_STORE_REVIEW(HttpStatus.NOT_FOUND, "S016", "해당 가게에 존재하는 리뷰가 아닙니다."),
    INVALID_STORE_MENU_UUID(HttpStatus.BAD_REQUEST, "S017", "유효하지 않은 메뉴 식별자입니다."),
    STORE_MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "S018", "존재하지 않는 메뉴입니다."),
    INVALID_STORE_MENU(HttpStatus.NOT_FOUND, "S019", "해당 가게에 존재하는 메뉴가 아닙니다."),
    STORE_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S020", "가게 서비스 처리 중 오류가 발생했습니다."),
    STORE_TAG_SAVE_FAILED(HttpStatus.BAD_REQUEST, "S021", "태그 저장에 실패했습니다."),
    STORE_MAP_READ_FAILED(HttpStatus.BAD_REQUEST, "S022", "반경 내 가게 조회에 실패했습니다."),
    STORE_SEARCH_FAILED(HttpStatus.BAD_REQUEST, "S023", "가게 검색에 실패했습니다."),
    PREFERENCE_STORE_READ_FAILED(HttpStatus.BAD_REQUEST, "S024", "반경 내 사용자 취향 맞춤 가게 조회에 실패했습니다."),
    STORE_INFO_READ_FAILED(HttpStatus.BAD_REQUEST, "S025", "가게 정보 조회에 실패했습니다."),
    STORE_RATE_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "S026", "가게 평균 평점 업데이트에 실패했습니다."),
    STORE_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "S027", "가게 수정에 실패했습니다."),
    STORE_DELETE_FAILED(HttpStatus.BAD_REQUEST, "S028", "가게 삭제에 실패했습니다."),
    USER_STORE_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S029", "가게 저장 서비스 처리 중 오류가 발생했습니다."),
    STORE_LIST_CREATION_FAILED(HttpStatus.BAD_REQUEST, "S030", "가게 저장 리스트 생성에 실패했습니다."),
    STORE_LIST_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "S031", "가게 저장 리스트 수정에 실패했습니다."),
    STORE_LIST_DELETE_FAILED(HttpStatus.BAD_REQUEST, "S032", "가게 저장 리스트 삭제에 실패했습니다."),
    STORE_SAVE_FAILED(HttpStatus.BAD_REQUEST, "S033", "가게 저장에 실패했습니다."),
    SAVED_STORE_DELETE_FAILED(HttpStatus.BAD_REQUEST, "S034", "가게 저장 취소에 실패했습니다."),
    STORE_REVIEW_CREATION_FAILED(HttpStatus.BAD_REQUEST, "S035", "한줄 리뷰 등록에 실패했습니다."),
    STORE_REVIEW_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "S036", "한줄 리뷰 수정에 실패했습니다."),
    STORE_REVIEW_DELETE_FAILED(HttpStatus.BAD_REQUEST, "S037", "한줄 리뷰 삭제에 실패했습니다."),
    STORE_REVIEW_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S038", "가게 한줄리뷰 서비스 처리 중 오류가 발생했습니다."),
    MENU_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S039", "가게 메뉴 서비스 처리 중 오류가 발생했습니다."),
    MENU_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "S041", "단일 메뉴 수정에 실패했습니다."),
    MENU_DELETE_FAILED(HttpStatus.BAD_REQUEST, "S042", "단일 메뉴 삭제에 실패했습니다."),
    MENU_CREATION_FAILED(HttpStatus.BAD_REQUEST, "S043", "메뉴 등록에 실패했습니다."),
    STORE_DUPLICATE_PRIMARY_LINK(HttpStatus.CONFLICT, "S044", "대표 링크는 하나만 설정할 수 있습니다."),
    STORE_NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "S045", "존재하지 않는 공지사항입니다."),
    STORE_NOTICE_CREATION_FAILED(HttpStatus.BAD_REQUEST, "S046", "공지사항 등록에 실패했습니다."),
    STORE_NOTICE_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "S047", "공지사항 수정에 실패했습니다."),
    STORE_NOTICE_DELETE_FAILED(HttpStatus.BAD_REQUEST, "S048", "공지사항 삭제에 실패했습니다."),
    STORE_NOTICE_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S049", "가게 공지사항 서비스 처리 중 오류가 발생했습니다."),


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

    // Image
    IMAGE_REFERENCE_INVALID(HttpStatus.BAD_REQUEST, "I002", "잘못된 이미지 참조 정보입니다."),
    IMAGE_FETCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "I003", "이미지 조회 중 오류가 발생했습니다."),

    //디저트메이트
    MATE_NOT_FOUND(HttpStatus.NOT_FOUND,"M001" ,"존재하지 않는 디저트메이트입니다." ),
    MATES_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "해당 범위에서 메이트 데이터를 찾을 수 없습니다."),
    INVALID_RANGE(HttpStatus.BAD_REQUEST, "M003", "잘못된 범위 요청입니다."),
    MATE_APPLY_WAIT(HttpStatus.CONFLICT, "M004", "메이트 신청 대기 중입니다."),
    MATE_APPLY_BANNED(HttpStatus.FORBIDDEN, "M005" , "디저트메이트 강퇴 당한 사람입니다. 신청 불가능합니다."),
    MATE_APPLY_REJECT(HttpStatus.FORBIDDEN, "M006" , "거절 된 메이트입니다. 신청 불가능합니다."),
    ALREADY_TEAM_MEMBER(HttpStatus.CONFLICT,"M007" , "해당 사용자는 이미 팀원입니다."),
    MATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "M008","메이트 관리자 권한이 없습니다."),
    MATE_REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "M009" , "존재하지 않는 댓글입니다."),
    MATE_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M010", "디저트메이트 멤버가 아닙니다."),
    REPLY_NOT_AUTHOR(HttpStatus.FORBIDDEN, "M011", "댓글 작성자가 아닙니다."),
    DUPLICATION_SAVED_STORE(HttpStatus.CONFLICT, "M012", "이미 저장된 디저트메이트입니다."),
    SAVED_MATE_NOT_FOUND(HttpStatus.NOT_FOUND,"M013" , "저장하지 않은 디저트메이트입니다."),
    MATE_RECRUIT_DONE(HttpStatus.FORBIDDEN,"M014" , "해당 디저트메이트 모집 마감입니다."),
    DUPLICATION_REPORT(HttpStatus.CONFLICT, "M015", "이미 신고된 게시물입니다."),
    MATE_NOT_PENDING_MEMBER(HttpStatus.NOT_FOUND, "M016",  "디저트메이트 신청하신 분이 아닙니다."),
    DUPLICATION_SAVED_MATE(HttpStatus.CONFLICT, "M017", "이미 저장된 디저트메이트입니다."),
    MATE_NOT_REPORTED(HttpStatus.NOT_FOUND, "M018" , "신고되지 않은 디저트메이트입니다." ),
    MATE_REPLY_NOT_REPORTED(HttpStatus.NOT_FOUND, "M019" , "신고되지 않은 디저트메이트 댓글입니다." ),
    MATE_CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "M020", "최대 수용 인원 초과입니다." ),

    //커뮤니티 리뷰
    COMMUNITY_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001" , "존재하지 않는 리뷰입니다." ),
    IMAGE_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "R002", "요청된 이미지 인덱스가 이미지 파일 수보다 적습니다."),
    DUPLICATE_DELETE_IMAGE_IDS(HttpStatus.BAD_REQUEST,"R003" , "중복된 이미지 삭제 ID가 존재합니다."),
    REVIEW_REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "R004", "존재하지 않는 리뷰 댓글입니다."),
    IMAGE_UUID_NOT_FOUND(HttpStatus.NOT_FOUND, "R005", "존재하지 않는 이미지 UUID 입니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.CONFLICT, "R006", "이미지 업로드 시 문제가 생겼습니다."),
    DUPLICATION_SAVED_REVIEW(HttpStatus.CONFLICT, "R007", "이미 저장된 커뮤니티 리뷰입니다."),
    SAVED_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R008", "저장된 커뮤니티 리뷰가 없습니다."),

    //관리자 페이지
    INVALID_YEAR(HttpStatus.BAD_REQUEST,"ADMIN_001","잘못된 연도입니다."),
    INVALID_MONTH(HttpStatus.BAD_REQUEST,"ADMIN_002","잘못된 월입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}