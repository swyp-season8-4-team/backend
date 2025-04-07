package org.swyp.dessertbee.community.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.community.review.dto.request.ReviewReplyCreateRequest;
import org.swyp.dessertbee.community.review.dto.response.ReviewReplyPageResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewReplyResponse;
import org.swyp.dessertbee.community.review.service.ReviewReplyService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "CommunityReply", description = "커뮤니티 리뷰 댓글 관련 API")
@RestController
@RequestMapping("/api/review/{reviewUuid}/reply")
@RequiredArgsConstructor
public class ReviewReplyController {


    private final ReviewReplyService reviewReplyService;


    /**
     * 커뮤니티 댓글 생성
     */
    @Operation(summary = "커뮤니티 리뷰 댓글 생성", description = "커뮤니티 리뷰에 댓글을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 리뷰 댓글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<ReviewReplyResponse> createReply(@RequestBody ReviewReplyCreateRequest request,
                                                           @PathVariable UUID reviewUuid) {

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewReplyService.createReply(reviewUuid, request));
    }

    /**
     * 커뮤니티 댓글 조회(한개만)
     * */
    @Operation(summary = "커뮤니티 리뷰 댓글 조회(한개만)", description = "커뮤니티 리뷰 댓글 Uuid에 맞는 하나의 댓글을 조회합니다..")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 리뷰 댓글 조회(한개만) 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.REVIEW_REPLY_NOT_FOUND})
    @GetMapping("/{reviewReplyUuid}")
    public ResponseEntity<ReviewReplyResponse> getReplyDetail(@PathVariable UUID reviewReplyUuid, @PathVariable UUID reviewUuid) {

        return ResponseEntity.ok(reviewReplyService.getReplyDetail(reviewUuid, reviewReplyUuid));
    }

    /**
     * 커뮤니티 댓글 전체 조회
     * */
    @Operation(summary = "커뮤니티 리뷰 댓글 전체 조회", description = "커뮤니티 리뷰에 댓글 전체 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 리뷰 댓글 전체 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.INVALID_RANGE})
    @GetMapping
    public ResponseEntity<ReviewReplyPageResponse> getReplies(@PathVariable UUID reviewUuid,
                                                              @RequestParam(required = false, defaultValue = "0") int from,
                                                              @RequestParam(required = false, defaultValue = "10") int to) {

        if (from >= to) {
            throw new BusinessException(ErrorCode.INVALID_RANGE);
        }

        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);


        return ResponseEntity.ok(reviewReplyService.getReplies(reviewUuid, pageable));

    }

    /**
     * 커뮤니티 댓글 수정
     * */
    @Operation(summary = "커뮤니티 리뷰 댓글 수정", description = "커뮤니티 리뷰에 댓글 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 리뷰 댓글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.REVIEW_REPLY_NOT_FOUND, ErrorCode.REPLY_NOT_AUTHOR})
    @PatchMapping("/{reviewReplyUuid}")
    public ResponseEntity<Map<String, String>> updateReply(
            @PathVariable UUID reviewUuid,
            @PathVariable UUID reviewReplyUuid,
            @RequestBody ReviewReplyCreateRequest request
    )
    {
        reviewReplyService.updateReply(reviewUuid, reviewReplyUuid, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 성공적으로 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 커뮤니티 댓글 삭제
     * */
    @Operation(summary = "커뮤니티 리뷰 댓글 삭제", description = "커뮤니티 리뷰에 댓글 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 리뷰 댓글 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ApiErrorResponses({ErrorCode.REVIEW_REPLY_NOT_FOUND, ErrorCode.REPLY_NOT_AUTHOR})
    @DeleteMapping("/{reviewReplyUuid}")
    public ResponseEntity<Map<String, String>> deleteReply(@PathVariable UUID reviewReplyUuid, @PathVariable UUID reviewUuid) {

        reviewReplyService.deleteReply(reviewUuid, reviewReplyUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 성공적으로 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }
}
