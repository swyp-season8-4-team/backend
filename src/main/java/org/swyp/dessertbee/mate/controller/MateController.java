package org.swyp.dessertbee.mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.mate.service.MateMemberService;
import org.swyp.dessertbee.mate.service.MateService;
import java.util.UUID;

@Tag(name = "Mate", description = "디저트메이트 관련 API")
@RestController
@RequestMapping("api/mates")
@RequiredArgsConstructor
public class MateController {

    private final MateService mateService;
    private final MateMemberService mateMemberService;
    private final ObjectMapper objectMapper;

    /**
     * 메이트 등록
     */
    @Operation(summary = "메이트 생성", description = "디저트메이트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MateDetailResponse> createMate(@RequestPart("request")  String requestJson,
                                                         @RequestPart(value = "mateImage", required = false) MultipartFile mateImage) {

        MateCreateRequest request;
        try {

            //JSON 문자열을 MateCreateRequest 객체로 변환
            request = objectMapper.readValue(requestJson, MateCreateRequest.class);


        }catch (Exception e) {
            e.printStackTrace();  // 에러 로그 출력
            return ResponseEntity.badRequest().body(null);
        }

        MateDetailResponse response = mateService.createMate(request, mateImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 메이트 상세 정보 조회
     */
    @Operation(summary = "메이트 상세 정보 조회", description = "디저트메이트 상세 정보 조회합니다.")
    @GetMapping("/{mateUuid}")
    public ResponseEntity<MateDetailResponse> getMateDetail(@PathVariable UUID mateUuid, UUID userUuid) {

        MateDetailResponse mate = mateService.getMateDetail(mateUuid, userUuid);
        return ResponseEntity.ok(mate);
    }


    /**
     * 메이트 삭제
     */
    @DeleteMapping("/{mateUuid}")
    @Operation(summary = "메이트 삭제", description = "디저트메이트 삭제합니다.")
    public ResponseEntity<String> deleteMate(@PathVariable UUID mateUuid) {
        //디저트메이트 삭제
        mateService.deleteMate(mateUuid);

        return ResponseEntity.ok("디저트메이트가 성공적으로 삭제되었습니다.");
    }

    /**
     * 메이트 수정
     * */
    @PatchMapping("/{mateUuid}")
    @Operation(summary = "메이트 수정", description = "디저트메이트 수정합니다.")
    public ResponseEntity<String> updateMate(
            @PathVariable UUID mateUuid,
            @RequestPart(value = "request") String requestJson,
            @RequestPart(value = "mateImage", required = false) MultipartFile mateImage
    ){

        ObjectMapper objectMapper = new ObjectMapper();
        MateCreateRequest request;


        try {

            //JSON 문자열을 MateCreateRequest 객체로 변환
            request = objectMapper.readValue(requestJson, MateCreateRequest.class);

        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }

        mateService.updateMate(mateUuid, request, mateImage);
        return ResponseEntity.ok("디저트메이트가 성공적으로 수정되었습니다.");
    }


    /**
     * 디저트메이트 전체 조회
     * */
    @GetMapping
    @Operation(summary = "메이트 전체 조회", description = "디저트메이트 전체 조회합니다.")
    public ResponseEntity<MatesPageResponse> getMates(
            @RequestParam int from,
            @RequestParam int to,
            UUID userUuid,
            Long mateCategoryId
    ) {

        if (from >= to) {
            throw new FromToMateException("잘못된 범위 요청입니다.");
        }


        Pageable pageable = PageRequest.of(from, to);
        return ResponseEntity.ok(mateService.getMates(pageable, userUuid, mateCategoryId));
    }
}
