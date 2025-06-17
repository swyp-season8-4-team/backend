package org.swyp.dessertbee.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.user.service.UserTestService;
import java.util.Map;

/**
 * 사용자 테스트용 컨트롤러
 * 릴리즈 환경에서만 활성화되며, 테스트 사용자 데이터 정리를 위한 Hard Delete 기능을 제공
 */
@RestController
@RequestMapping("/api/users/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Test", description = "사용자 테스트 API (릴리즈 환경 전용)")
public class UserTestController {

    private final UserTestService userTestService;

    @Operation(
            summary = "현재 로그인 사용자 완전 삭제 (Hard Delete)",
            description = "JWT 토큰 기반으로 현재 로그인한 사용자와 관련된 모든 데이터를 완전히 삭제합니다. 관리자 권한 필요."
    )
    @DeleteMapping("/me/hard-delete")
    public ResponseEntity<Map<String, String>> hardDeleteMyAccount() {

        log.warn("현재 로그인 사용자 테스트용 Hard Delete API 호출");

        String deletedEmail = userTestService.hardDeleteCurrentUserWithAllRelatedData();

        return ResponseEntity.ok(
                Map.of("message", "현재 사용자가 완전히 삭제되었습니다.", "email", deletedEmail)
        );
    }
}