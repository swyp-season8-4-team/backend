package org.swyp.dessertbee.community.mate.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.community.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.community.mate.service.SavedMateService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "SavedMate", description = "디저트메이트 저장 관련 API")
@RestController
@RequestMapping("api/mates/saved")
@RequiredArgsConstructor
public class SavedMateController {

    private final SavedMateService savedMateService;

    /**
     * 디저트메이트 저장
     * */
    @Operation(summary = "디저트메이트 저장", description = "디저트메이트 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.MATE_NOT_FOUND, ErrorCode.DUPLICATION_SAVED_MATE, ErrorCode.USER_NOT_FOUND})
    @PostMapping("/{mateUuid}")
    public ResponseEntity<Map<String, String>> saveMate(@PathVariable UUID mateUuid) {


        savedMateService.saveMate(mateUuid);


        Map<String, String> response = new HashMap<>();
        response.put("message", "디저트메이트가 성공적으로 저장되었습니다.");

        return ResponseEntity.ok(response);
    }


    /**
     * 디저트메이트 저장 삭제
     * */
    @Operation(summary = "디저트메이트 저장 삭제", description = "저장한 디저트메이트 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 저장 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.MATE_NOT_FOUND, ErrorCode.SAVED_MATE_NOT_FOUND, ErrorCode.USER_NOT_FOUND})
    @DeleteMapping("/{mateUuid}")
    public ResponseEntity<Map<String, String>> deleteSavedMate(@PathVariable UUID mateUuid) {

        savedMateService.deleteSavedMate(mateUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "디저트메이트가 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 저장된 디저트메이트 조회
     * */
    @Operation(summary = "저장된 디저트메이트 조회", description = "저장된 디저트메이트 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장된 디저트메이트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_RANGE})
    @GetMapping
    private ResponseEntity<MatesPageResponse> getSavedMates(
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int to
    ){

        if (from >= to) {
            throw new BusinessException(ErrorCode.INVALID_RANGE);
        }

        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(savedMateService.getSavedMates(pageable));
    }
}
