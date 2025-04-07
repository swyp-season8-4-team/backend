package org.swyp.dessertbee.preference.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.preference.dto.PreferenceResponseDto;
import org.swyp.dessertbee.preference.service.PreferenceService;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@Tag(name = "Preference", description = "선호도 관련 API")
public class PreferenceController {

    private final PreferenceService preferenceService;

    /**
     * 모든 선호도 정보를 조회하는 API 엔드포인트
     * @return HTTP 200 OK와 함께 선호도 정보 리스트 반환
     * @apiNote GET /api/preferences
     */
    @Operation(summary = "모든 선호도 조회 (completed)", description = "시스템에 등록된 모든 선호도 정보를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "선호도 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PreferenceResponseDto.class))
            )
    )
    @ApiErrorResponses({
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping
    public ResponseEntity<List<PreferenceResponseDto>> getAllPreferences() {
        List<PreferenceResponseDto> preferences = preferenceService.getAllPreferences();
        return ResponseEntity.ok(preferences);  // HTTP 상태코드 200과 함께 데이터 반환
    }
}