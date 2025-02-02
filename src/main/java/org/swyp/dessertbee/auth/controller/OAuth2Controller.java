package org.swyp.dessertbee.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "소셜 로그인 관련 API")
public class OAuth2Controller {

    @Operation(summary = "카카오 로그인 인증", description = "카카오 OAuth2 인증 프로세스를 시작합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "카카오 로그인 페이지로 리다이렉트")
    })
    @GetMapping("/authorization/{provider}")
    public void socialLogin(
            @Parameter(description = "소셜 로그인 제공자 (ex: kakao)", required = true)
            @PathVariable String provider) {
        // OAuth2 인증 처리는 Spring Security에서 자동으로 처리됨
    }
}