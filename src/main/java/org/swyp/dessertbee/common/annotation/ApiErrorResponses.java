package org.swyp.dessertbee.common.annotation;

import org.swyp.dessertbee.common.exception.ErrorCode;
import java.lang.annotation.*;

/**
 * API 메서드에서 발생할 수 있는 에러 코드들을 명시하는 어노테이션
 * 이 어노테이션을 사용해 선언된 에러 코드들은 Swagger 문서에 자동으로 에러 응답 예제로 추가됨
 * 컨트롤러 메서드 위에 달면 됨.
 *
 * 사용 예는 아래와 같음.
 * @ApiErrorResponses({ErrorCode.PASSWORD_MISMATCH, ErrorCode.USER_NOT_FOUND})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiErrorResponses {
    ErrorCode[] value();
}
