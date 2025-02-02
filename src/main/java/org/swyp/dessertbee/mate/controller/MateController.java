package org.swyp.dessertbee.mate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.mate.service.MateService;
import org.swyp.dessertbee.store.dto.response.StoreDetailResponse;

import java.util.List;

@RestController
@RequestMapping("api/mates")
@RequiredArgsConstructor
public class MateController {

    private final MateService mateService;

    /** 메이트 등록 */
     @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MateDetailResponse> createMate(@RequestPart("request") @Valid MateCreateRequest request,
                                                         @RequestPart(value = "mateImageFiles", required = false) List<MultipartFile> mateImageFiles){
         return ResponseEntity.status(HttpStatus.CREATED).body(mateService.createMate(request, mateImageFiles));
     }

     /** 메이트 상세 정보 조회 */
     @GetMapping("/{mateId}/details")
    public MateDetailResponse getMateDetails(@PathVariable Long mateId){
         return mateService.getMateDetails(mateId);
     }
}
