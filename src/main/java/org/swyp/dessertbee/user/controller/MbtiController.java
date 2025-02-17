package org.swyp.dessertbee.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.user.dto.MbtiResponseDto;
import org.swyp.dessertbee.user.service.MbtiService;

import java.util.List;

@RestController
@RequestMapping("/api/mbtis")
@RequiredArgsConstructor
public class MbtiController {

    private final MbtiService mbtiService;

    /**
     * 모든 MBTI 정보를 조회하는 API 엔드포인트
     */
    @GetMapping
    public ResponseEntity<List<MbtiResponseDto>> getAllMbtis() {
        return ResponseEntity.ok(mbtiService.getAllMbtis());
    }

    /**
     * 특정 ID의 MBTI 정보를 조회하는 API 엔드포인트
     * @param id MBTI ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MbtiResponseDto> getMbtiById(@PathVariable Long id) {
        return ResponseEntity.ok(mbtiService.getMbtiById(id));
    }

    /**
     * MBTI 유형으로 MBTI 정보를 조회하는 API 엔드포인트
     *
     * @param mbtiType MBTI 유형 문자열
     */
    @GetMapping("/type/{mbtiType}")
    public ResponseEntity<MbtiResponseDto> getMbtiByType(@PathVariable String mbtiType) {
        return ResponseEntity.ok(mbtiService.getMbtiByType(mbtiType));
    }
}