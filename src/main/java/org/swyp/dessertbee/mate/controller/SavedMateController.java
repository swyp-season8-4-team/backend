package org.swyp.dessertbee.mate.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.mate.service.MateService;
import org.swyp.dessertbee.mate.service.SavedMateService;

import java.util.UUID;

@Tag(name = "SavedMate", description = "디저트메이트 저장 관련 API")
@RestController
@RequestMapping("api/mates/saved")
@RequiredArgsConstructor
public class SavedMateController {

    private final SavedMateService savedMateService;
    private final MateService mateService;

    /**
     * 디저트메이트 저장
     * */
    @PostMapping("/{mateUuid}")
    public ResponseEntity<String> saveMate(@PathVariable UUID mateUuid, UUID userUuid) {


        savedMateService.saveMate(mateUuid, userUuid);


        return ResponseEntity.ok("디저트메이트가 성공적으로 저장되었습니다.");
    }


    /**
     * 디저트메이트 삭제
     * */
    @DeleteMapping("/{mateUuid}")
    public ResponseEntity<String> deleteSavedMate(@PathVariable UUID mateUuid, UUID userUuid) {

        savedMateService.deleteSavedMate(mateUuid, userUuid);

        return ResponseEntity.ok("디저트메이트가 성공적으로 삭제되었습니다.");
    }

    /**
     * 저장된 디저트메이트 조회
     * */
    @GetMapping
    private ResponseEntity<MatesPageResponse> getSavedMates(
            @RequestParam int from,
            @RequestParam int to,
            UUID userUuid
    ){

        if (from >= to) {
            throw new FromToMateException("잘못된 범위 요청입니다.");
        }

        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(savedMateService.getSavedMates(pageable, userUuid));
    }
}
