package org.swyp.dessertbee.community.review.controller;


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
import org.swyp.dessertbee.community.review.dto.response.ReviewPageResponse;
import org.swyp.dessertbee.community.review.service.SavedReviewService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "SavedCommunityReview", description = "커뮤니티 리뷰 저장 관련 API")
@RestController
@RequestMapping("/api/review/saved")
@RequiredArgsConstructor
public class SavedReviewController {

    private final SavedReviewService savedReviewService;

    /**
     * 커뮤니티 리뷰 저장
     * */
    @Operation(summary = "커뮤니티 리뷰 저장", description = "커뮤니티 리뷰 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 리뷰 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.COMMUNITY_REVIEW_NOT_FOUND, ErrorCode.DUPLICATION_SAVED_REVIEW, ErrorCode.USER_NOT_FOUND})
    @PostMapping("/{reviewUuid}")
    public ResponseEntity<Map<String, String>> saveReview(@PathVariable UUID reviewUuid){

        savedReviewService.saveReview(reviewUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "커뮤니티 리뷰 저장이 성공적으로 저장되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 커뮤니티 리뷰 삭제
     * */
    @Operation(summary = "저장된 커뮤니티 리뷰 삭제", description = "저장된 커뮤니티 리뷰 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장된 커뮤니티 리뷰 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.COMMUNITY_REVIEW_NOT_FOUND, ErrorCode.SAVED_REVIEW_NOT_FOUND, ErrorCode.USER_NOT_FOUND})
    @DeleteMapping("/{reviewUuid}")
    public ResponseEntity<Map<String, String>> deleteSavedReview(@PathVariable UUID reviewUuid){

        savedReviewService.deleteSavedReview(reviewUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "저장된 커뮤니티 리뷰가 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "저장된 커뮤니티 리뷰 전체 조회", description = "저장된 커뮤니티 리뷰 전체 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장된 커뮤니티 리뷰 전젳 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.INVALID_RANGE})
    @GetMapping
    public ResponseEntity<ReviewPageResponse> getSavedReviews(@RequestParam(required = false, defaultValue = "0") int from,
                                                              @RequestParam(required = false, defaultValue = "10") int to){

        if (from >= to) {
            throw new BusinessException(ErrorCode.INVALID_RANGE);
        }

        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);


        return ResponseEntity.ok(savedReviewService.getSavedReviews(pageable));

    }
}
