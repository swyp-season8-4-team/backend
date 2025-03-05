package org.swyp.dessertbee.mate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.mate.dto.request.MateReplyCreateRequest;
import org.swyp.dessertbee.mate.dto.request.MateReportRequest;
import org.swyp.dessertbee.mate.dto.response.MateReplyPageResponse;
import org.swyp.dessertbee.mate.dto.response.MateReplyResponse;
import org.swyp.dessertbee.mate.exception.MateExceptions;
import org.swyp.dessertbee.mate.service.MateReplyService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "MateReply", description = "디저트메이트 댓글 관련 API")
@RestController
@RequestMapping("/api/mates/{mateUuid}/reply")
@RequiredArgsConstructor
public class MateReplyController {

    private final MateReplyService mateReplyService;

    /**
     * 디저트메이트 댓글 생성
     * */
    @Operation(summary = "메이트 댓글 생성", description = "디저트메이트를 댓글을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 댓글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<MateReplyResponse> createReply(@RequestBody  MateReplyCreateRequest request,
                                                         @PathVariable UUID mateUuid) {

        MateReplyResponse response = mateReplyService.createReply(mateUuid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }



    /**
     * 디저트메이트 댓글 조회(한개만)
     * */
    @GetMapping("/{replyId}")
    public ResponseEntity<MateReplyResponse> getReplyDetail(@PathVariable UUID mateUuid, @PathVariable Long replyId) {

        MateReplyResponse response = mateReplyService.getReplyDetail(mateUuid, replyId);


        return ResponseEntity.ok(response);
    }

    /**
     * 디저트메이트 댓글 전체 조회
     * */
    @GetMapping
    public ResponseEntity<MateReplyPageResponse> getReplies(@PathVariable UUID mateUuid,
                                                            @RequestParam(required = false, defaultValue = "0") int from,
                                                            @RequestParam(required = false, defaultValue = "10") int to) {

        if (from >= to) {
            throw new MateExceptions.FromToMateException("잘못된 범위 요청입니다.");
        }

        int size = to - from;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(mateReplyService.getReplies(mateUuid, pageable));
    }

    /**
     * 디저트메이트 댓글 수정
     * */
    @PatchMapping("/{replyId}")
    public ResponseEntity<Map<String, String>>  updateReply(
            @PathVariable UUID mateUuid,
            @PathVariable Long replyId,
            @RequestBody MateReplyCreateRequest request){


        mateReplyService.updateReply(mateUuid, replyId, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 성공적으로 수정되었습니다.");
        return ResponseEntity.ok(response);

    }

    /**
     * 디저트메이트 댓글 삭제
     * */
    @DeleteMapping("/{replyId}")
    public ResponseEntity<Map<String, String>> deleteReply(@PathVariable UUID mateUuid,
                                              @PathVariable Long replyId) {

        mateReplyService.deleteReply(mateUuid, replyId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 성공적으로 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 디저트메이트 댓글 신고
     * */
    @PostMapping("/{replyId}/report")
    public ResponseEntity<Map<String, String>>  reportMateReply(@PathVariable UUID mateUuid,
                                                  @PathVariable Long replyId,
                                                  @RequestBody  MateReportRequest request) {

        mateReplyService.reportMateReply(mateUuid, replyId, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글 신고 되었습니다.");

        return ResponseEntity.ok(response);
    }
}
