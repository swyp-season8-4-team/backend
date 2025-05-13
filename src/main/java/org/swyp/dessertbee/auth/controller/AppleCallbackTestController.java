package org.swyp.dessertbee.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Apple 로그인 콜백을 긴급 처리하기 위한 임시 컨트롤러
 * 테스트 용도로만 사용
 */
@Controller
@RequestMapping("/ko/oauth/callback")
@Slf4j
public class AppleCallbackTestController {

    /**
     * Apple 로그인 콜백 처리 - Form POST 방식으로 받은 데이터를 로깅하고 HTML 응답 제공
     */
    @PostMapping("/apple")
    public String handleAppleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "id_token", required = false) String idToken,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "user", required = false) String user,
            HttpServletRequest request
    ) {
        log.info("Apple 로그인 콜백 데이터 수신 (EmergencyCallbackController)");
        log.info("code: {}", code);
        log.info("id_token: {}", idToken);
        log.info("state: {}", state);
        log.info("user: {}", user);

        // 모든 요청 파라미터 로깅
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            log.info("Parameter: {} = {}", paramName, request.getParameter(paramName));
        }

        // 테스트 페이지로 리다이렉트하면서 파라미터 전달
        return "redirect:/apple-oauth-test.html?code=" + code +
                (idToken != null ? "&id_token=" + idToken : "") +
                (state != null ? "&state=" + state : "") +
                (user != null ? "&user=" + user : "");
    }

    /**
     * Apple 로그인 콜백 처리 - JSON API 버전
     * 이 엔드포인트는 프론트엔드에서 직접 호출할 수 있음
     */
    @PostMapping(value = "/apple/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAppleCallbackApi(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "id_token", required = false) String idToken,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "user", required = false) String user,
            HttpServletRequest request
    ) {
        log.info("Apple 로그인 API 콜백 수신");
        log.info("code: {}", code);
        log.info("id_token: {}", idToken);
        log.info("state: {}", state);
        log.info("user: {}", user);

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Apple 로그인 콜백이 성공적으로 처리되었습니다.");
        response.put("code", code);
        response.put("id_token", idToken);
        response.put("state", state);
        response.put("user", user);

        return ResponseEntity.ok(response);
    }
}