package org.swyp.dessertbee.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 헬스체크 전용 컨트롤러 (HTTP 테스트용)
 */
@RestController
public class HealthController {

    /**
     * 기본 헬스체크 - 서버 기동 확인용
     * GET /health
     */
    @GetMapping("/api/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}