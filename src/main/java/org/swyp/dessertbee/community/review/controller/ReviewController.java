package org.swyp.dessertbee.community.review.controller;


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
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.community.review.dto.request.ReviewCreateRequest;
import org.swyp.dessertbee.community.review.dto.request.ReviewUpdateRequest;
import org.swyp.dessertbee.community.review.dto.response.ReviewPageResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewResponse;
import org.swyp.dessertbee.community.review.service.ReviewService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "CommunityReview", description = "커뮤니티 리뷰 관련 API")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;


    /**
     * 커뮤니티 리뷰 등록
     * */
    @Operation(summary = "커뮤니티 리뷰 생성", description = "커뮤니티 맛집 리뷰를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 리뷰 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponse> createReview(@RequestPart("request") ReviewCreateRequest request,
                                                       @RequestPart(value = "reviewImages", required = false) List<MultipartFile> reviewImages){

        ReviewResponse response = reviewService.createReview(request, reviewImages);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 커뮤니티 리뷰 상세 조회
     * */
    @Operation(summary = "커뮤니티 리뷰 상세 조회", description = "커뮤니티 맛집 리뷰 상세 조회합니다.")
    @GetMapping("/{reviewUuid}")
    public ResponseEntity<ReviewResponse> getReviewDetail(@PathVariable UUID reviewUuid){

        ReviewResponse response = reviewService.getReviewDetail(reviewUuid);

        return ResponseEntity.ok(response);
    }

    /**
     * 커뮤니티 리뷰 전체 조회
     * */
    @GetMapping()
    public ResponseEntity<ReviewPageResponse> getReviews(
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int to,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) Long reviewCategoryId
    ) {

        if (from >= to) {
            throw new BusinessException(ErrorCode.INVALID_RANGE);
        }

        if(keyword != null) {
            keyword = URLDecoder.decode(keyword, StandardCharsets.UTF_8);
        }
        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(reviewService.getReviews(pageable, keyword, reviewCategoryId));
    }

    /**
     * 커뮤니티 리뷰 수정
     * */
    @PatchMapping("/{reviewUuid}")
    public ResponseEntity<Map<String, String>> updateReview(
            @PathVariable UUID reviewUuid,
            @RequestPart("request") ReviewUpdateRequest request,
            @RequestPart(value = "reviewImages", required = false) List<MultipartFile> reviewImages
    ){
        reviewService.updateReview(reviewUuid, request, reviewImages);

        Map<String, String> response = new HashMap<>();
        response.put("message", "리뷰가 성공적으로 수정되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**커뮤니티 리뷰 삭제*/
    @DeleteMapping("/{reviewUuid}")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable UUID reviewUuid){

        reviewService.deleteReview(reviewUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "리뷰가 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }
}
