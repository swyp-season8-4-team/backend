package org.swyp.dessertbee.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.user.dto.response.MbtiResponseDto;
import org.swyp.dessertbee.user.service.MbtiService;

import java.util.List;

@RestController
@RequestMapping("/api/mbtis")
@RequiredArgsConstructor
@Tag(name = "MBTI", description = "MBTI 관련 API")
public class MbtiController {

    private final MbtiService mbtiService;

    /**
     * 모든 MBTI 정보를 조회하는 API 엔드포인트
     */
    @Operation(summary = "모든 MBTI 조회", description = "시스템에 등록된 모든 MBTI 정보를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "MBTI 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = MbtiResponseDto.class))
            )
    )
    @GetMapping
    public ResponseEntity<List<MbtiResponseDto>> getAllMbtis() {
        return ResponseEntity.ok(mbtiService.getAllMbtis());
    }

    /**
     * 특정 ID의 MBTI 정보를 조회하는 API 엔드포인트
     * @param id MBTI ID
     */
    @Operation(summary = "ID로 MBTI 조회", description = "특정 ID의 MBTI 정보를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "MBTI 조회 성공",
            content = @Content(schema = @Schema(implementation = MbtiResponseDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "MBTI를 찾을 수 없음",
            content = @Content
    )
    @GetMapping("/{id}")
    public ResponseEntity<MbtiResponseDto> getMbtiById(
            @Parameter(description = "MBTI ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(mbtiService.getMbtiById(id));
    }

    /**
     * MBTI 유형으로 MBTI 정보를 조회하는 API 엔드포인트
     * @param mbtiType MBTI 유형 문자열
     */
    @Operation(summary = "유형으로 MBTI 조회", description = "MBTI 유형(예: ENFP, INTJ)으로 MBTI 정보를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "MBTI 조회 성공",
            content = @Content(schema = @Schema(implementation = MbtiResponseDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "MBTI 유형을 찾을 수 없음",
            content = @Content
    )
    @GetMapping("/type/{mbtiType}")
    public ResponseEntity<MbtiResponseDto> getMbtiByType(
            @Parameter(description = "MBTI 유형", required = true, example = "ENFP")
            @PathVariable String mbtiType) {
        return ResponseEntity.ok(mbtiService.getMbtiByType(mbtiType));
    }
}