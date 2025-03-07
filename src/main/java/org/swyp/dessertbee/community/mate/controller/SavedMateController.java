package org.swyp.dessertbee.community.mate.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.community.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
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
    @PostMapping("/{mateUuid}")
    public ResponseEntity<Map<String, String>> saveMate(@PathVariable UUID mateUuid) {


        savedMateService.saveMate(mateUuid);


        Map<String, String> response = new HashMap<>();
        response.put("message", "디저트메이트가 성공적으로 저장되었습니다.");

        return ResponseEntity.ok(response);
    }


    /**
     * 디저트메이트 삭제
     * */
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
    @GetMapping
    private ResponseEntity<MatesPageResponse> getSavedMates(
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "0") int to
    ){

        if (from >= to) {
            throw new FromToMateException("잘못된 범위 요청입니다.");
        }

        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(savedMateService.getSavedMates(pageable));
    }
}
