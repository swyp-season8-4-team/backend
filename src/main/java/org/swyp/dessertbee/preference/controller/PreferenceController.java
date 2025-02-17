package org.swyp.dessertbee.preference.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.preference.dto.PreferenceResponseDto;
import org.swyp.dessertbee.preference.service.PreferenceService;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    /**
     * 모든 선호도 정보를 조회하는 API 엔드포인트
     * @return HTTP 200 OK와 함께 선호도 정보 리스트 반환
     * @apiNote GET /api/preferences
     */
    @GetMapping
    public ResponseEntity<List<PreferenceResponseDto>> getAllPreferences() {
        List<PreferenceResponseDto> preferences = preferenceService.getAllPreferences();
        return ResponseEntity.ok(preferences);  // HTTP 상태코드 200과 함께 데이터 반환
    }
}