package org.swyp.dessertbee.community.mate.controller;

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
import org.swyp.dessertbee.community.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateReportRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.community.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.community.mate.service.MateService;
import org.swyp.dessertbee.user.service.UserServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Mate", description = "디저트메이트 관련 API")
@RestController
@RequestMapping("api/mates")
@RequiredArgsConstructor
public class MateController {

    private final MateService mateService;
    private final UserServiceImpl userService;

    /**
     * 메이트 등록
     */
    @Operation(summary = "메이트 생성", description = "디저트메이트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MateDetailResponse> createMate(@RequestPart("request")  MateCreateRequest request,
                                                         @RequestPart(value = "mateImage", required = false) MultipartFile mateImage) {


        MateDetailResponse response = mateService.createMate(request, mateImage);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 메이트 상세 정보 조회
     */
    @Operation(summary = "메이트 상세 정보 조회", description = "디저트메이트 상세 정보 조회합니다.")
//    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{mateUuid}")
    public ResponseEntity<MateDetailResponse> getMateDetail(@PathVariable UUID mateUuid) {

        MateDetailResponse mate = mateService.getMateDetail(mateUuid);
        return ResponseEntity.ok(mate);
    }


    /**
     * 메이트 삭제
     */
    @DeleteMapping("/{mateUuid}")
    @Operation(summary = "메이트 삭제", description = "디저트메이트 삭제합니다.")
    public ResponseEntity<Map<String, String>> deleteMate(@PathVariable UUID mateUuid) {
        //디저트메이트 삭제
        mateService.deleteMate(mateUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "디저트메이트가 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 메이트 수정
     * */
    @PatchMapping("/{mateUuid}")
    @Operation(summary = "메이트 수정", description = "디저트메이트 수정합니다.")
    public ResponseEntity<Map<String, String>> updateMate(
            @PathVariable UUID mateUuid,
            @RequestPart(value = "request") MateCreateRequest request,
            @RequestPart(value = "mateImage", required = false) MultipartFile mateImage
    ){
        mateService.updateMate(mateUuid, request, mateImage);


        Map<String, String> response = new HashMap<>();
        response.put("message", "디저트메이트가 성공적으로 저장되었습니다.");

        return ResponseEntity.ok(response);
    }


    /**
     * 디저트메이트 전체 조회
     * */
    @GetMapping
    @Operation(summary = "메이트 전체 조회", description = "디저트메이트 전체 조회합니다.")
//    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<MatesPageResponse> getMates(
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int to,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) Long mateCategoryId
    ) {

        if (from >= to) {
            throw new FromToMateException("잘못된 범위 요청입니다.");
        }

        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(mateService.getMates(pageable, keyword, mateCategoryId));
    }

    /**
     * 내가 참여한 디저트메이트 조회
     * */
    @GetMapping("/me")
//    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<MatesPageResponse> getMyMates( @RequestParam(required = false, defaultValue = "0") int from,
                                                         @RequestParam(required = false, defaultValue = "10") int to){

        if (from >= to) {
            throw new FromToMateException("잘못된 범위 요청입니다.");
        }

        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);


        return ResponseEntity.ok(mateService.getMyMates(pageable));
    }

    /**
     * 디저트메이트 신고 기능
     * */
    @PostMapping("/{mateUuid}/report")
    public ResponseEntity<Map<String, String>> reportMate(@PathVariable UUID mateUuid,
                                             @RequestBody  MateReportRequest request) {

        mateService.reportMate(mateUuid, request);


        Map<String, String> response = new HashMap<>();
        response.put("message", "신고 되었습니다.");


        return ResponseEntity.ok(response);
    }

}
