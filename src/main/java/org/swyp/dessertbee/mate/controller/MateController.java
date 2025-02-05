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
import org.swyp.dessertbee.mate.service.MateService;

import java.util.List;

@Tag(name = "Mate", description = "디저트메이트 관련 API")
@RestController
@RequestMapping("api/mates")
@RequiredArgsConstructor
public class MateController {

    private final MateService mateService;

    /** 메이트 등록 */
    @Operation(summary = "메이트 생성", description = "디저트메이트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MateDetailResponse> createMate(@RequestPart("request") @Valid MateCreateRequest request,
                                                         @RequestPart(value = "mateImageFiles", required = false) List<MultipartFile> mateImageFiles){
         return ResponseEntity.status(HttpStatus.CREATED).body(mateService.createMate(request, mateImageFiles));
     }

     /** 메이트 상세 정보 조회 */
     @Operation(summary = "메이트 상세 정보 조회", description = "디저트메이트 상세 정보 조회합니다.")
     @GetMapping("/{mateId}/details")
    public MateDetailResponse getMateDetails(@PathVariable Long mateId){
         return mateService.getMateDetails(mateId);
     }


     /** 메이트 삭제 */
     @DeleteMapping("{mateId}")
    public ResponseEntity<Void> deleteMate(@PathVariable Long mateId){
         mateService.deleteMate(mateId);

         return ResponseEntity.ok().build();
     }

     /** 메이트 수정 */

}
