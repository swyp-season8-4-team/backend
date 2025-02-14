package org.swyp.dessertbee.mate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.mate.service.MateMemberService;
import org.swyp.dessertbee.mate.service.MateService;
import java.util.List;
import java.util.UUID;

@Tag(name = "Mate", description = "디저트메이트 관련 API")
@RestController
@RequestMapping("api/mates")
@RequiredArgsConstructor
public class MateController {

    private final MateService mateService;
    private final MateMemberService mateMemberService;

    /**
     * 메이트 등록
     */
    @Operation(summary = "메이트 생성", description = "디저트메이트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MateDetailResponse> createMate(@RequestPart("request") @Valid MateCreateRequest request,
                                                         @RequestPart(value = "mateImage", required = false) List<MultipartFile> mateImage) {

        return ResponseEntity.status(HttpStatus.CREATED).body(mateService.createMate(request, mateImage));
    }

    /**
     * 메이트 상세 정보 조회
     */
    @Operation(summary = "메이트 상세 정보 조회", description = "디저트메이트 상세 정보 조회합니다.")
    @GetMapping("/{mateUuid}/details")
    public MateDetailResponse getMateDetails(@PathVariable UUID mateUuid) {


        return mateService.getMateDetails(mateUuid);
    }


    /**
     * 메이트 삭제
     */
    @DeleteMapping("/{mateUuid}")
    @Operation(summary = "메이트 삭제", description = "디저트메이트 삭제합니다.")
    public ResponseEntity<String> deleteMate(@PathVariable UUID mateUuid) {
        //디저트메이트 삭제
        mateService.deleteMate(mateUuid);

        //디저트메이트 멤버 삭제
        mateMemberService.deleteAllMember(mateUuid);
        return ResponseEntity.ok("디저트메이트가 성공적으로 삭제되었습니다.");
    }

    /**
     * 메이트 수정
     * */
    @PatchMapping("/{mateUuid}")
    @Operation(summary = "메이트 수정", description = "디저트메이트 수정합니다.")
    public ResponseEntity<String> updateMate(
            @PathVariable UUID mateUuid,
            @RequestPart("request") MateCreateRequest request,
            @RequestPart(value = "mateImage", required = false) MultipartFile mateImage
    ){

        mateService.updateMate(mateUuid, request, mateImage);
        return ResponseEntity.ok("디저트메이트가 성공적으로 수정되었습니다.");
    }


    /**
     * 디저트메이트 전체 조회
     * */
    @GetMapping
    @Operation(summary = "메이트 전체 조회", description = "디저트메이트 전체 조회합니다.")
    public ResponseEntity<List<MateDetailResponse>> getMates(
            @RequestParam int from,
            @RequestParam int to
    ) {

        if (from >= to) {
            throw new IllegalArgumentException("잘못된 범위 설정");
        }


        return ResponseEntity.ok(mateService.getMates(from, to));
    }
}
