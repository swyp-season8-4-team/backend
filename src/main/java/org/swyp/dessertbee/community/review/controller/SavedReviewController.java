package org.swyp.dessertbee.community.review.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    @DeleteMapping("/{reviewUuid}")
    public ResponseEntity<Map<String, String>> deleteSavedReview(@PathVariable UUID reviewUuid){

        savedReviewService.deleteSavedReview(reviewUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "커뮤니티 리뷰 저장이 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }
}
