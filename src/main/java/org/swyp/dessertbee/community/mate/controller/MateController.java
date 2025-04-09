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
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.community.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateReportRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.community.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.community.mate.service.MateService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Mate", description = "디저트메이트 관련 API")
@RestController
@RequestMapping("api/mates")
@RequiredArgsConstructor
public class MateController{

    private final MateService mateService;

    /**
     * 메이트 등록
     */
    @Operation(summary = "메이트 생성(completed)", description = "디저트메이트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "디저트메이트 생성 성공")
    })
    @ApiErrorResponses({ErrorCode.USER_NOT_FOUND})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MateDetailResponse> createMate(@RequestPart("request")  MateCreateRequest request,
                                                         @RequestPart(value = "mateImage", required = false) MultipartFile mateImage) {


        MateDetailResponse response = mateService.createMate(request, mateImage);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 메이트 상세 정보 조회
     */
    @Operation(summary = "메이트 상세 정보 조회(completed)", description = "디저트메이트 상세 정보 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 상세 정보 요청 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_NOT_FOUND})
    @GetMapping("/{mateUuid}")
    public ResponseEntity<MateDetailResponse> getMateDetail(@PathVariable UUID mateUuid) {

        MateDetailResponse mate = mateService.getMateDetail(mateUuid);
        return ResponseEntity.ok(mate);
    }


    /**
     * 메이트 삭제
     */
    @Operation(summary = "메이트 삭제(completed)", description = "디저트메이트 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트메이트 삭제 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_NOT_FOUND})
    @DeleteMapping("/{mateUuid}")
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
    @Operation(summary = "메이트 수정(completed)", description = "디저트메이트 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트메이트 수정 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_NOT_FOUND})
    @PatchMapping("/{mateUuid}")
    public ResponseEntity<Map<String, String>> updateMate(
            @PathVariable UUID mateUuid,
            @RequestPart(value = "request") MateCreateRequest request,
            @RequestPart(value = "mateImage", required = false) MultipartFile mateImage
    ){
        mateService.updateMate(mateUuid, request, mateImage);


        Map<String, String> response = new HashMap<>();
        response.put("message", "디저트메이트가 성공적으로 수정되었습니다.");

        return ResponseEntity.ok(response);
    }


    /**
     * 디저트메이트 전체 조회
     * */
    @Operation(summary = "메이트 전체 조회(completed)", description = "디저트메이트 전체 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 전체 조회 성공")
    })
    @ApiErrorResponses({ErrorCode.INVALID_RANGE})
    @GetMapping
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

        if(keyword != null) {
            keyword = URLDecoder.decode(keyword, StandardCharsets.UTF_8);
        }
        return ResponseEntity.ok(mateService.getMates(pageable, keyword, mateCategoryId));
    }

    /**
     * 내가 참여한 디저트메이트 조회
     * */
    @Operation(summary = "내가 참여한 디저트메이트 조회(completed)", description = "내가 참여한 디저트메이트 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내가 참여한 디저트메이트 조회 성공")
    })
    @ApiErrorResponses({ErrorCode.INVALID_RANGE})
    @GetMapping("/me")
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
    @Operation(summary = "디저트메이트 신고 기능(completed)", description = "디저트메이트 신고합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트메이트 신고 기능")
    })
    @ApiErrorResponses({ErrorCode.DUPLICATION_REPORT})
    @PostMapping("/{mateUuid}/report")
    public ResponseEntity<Map<String, String>> reportMate(@PathVariable UUID mateUuid,
                                             @RequestBody  MateReportRequest request) {

        mateService.reportMate(mateUuid, request);


        Map<String, String> response = new HashMap<>();
        response.put("message", "신고 되었습니다.");


        return ResponseEntity.ok(response);
    }

}
