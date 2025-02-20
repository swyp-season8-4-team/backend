package org.swyp.dessertbee.mate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.mate.dto.request.MateReplyCreateRequest;
import org.swyp.dessertbee.mate.dto.response.MateReplyPageResponse;
import org.swyp.dessertbee.mate.dto.response.MateReplyResponse;
import org.swyp.dessertbee.mate.service.MateReplyService;

import java.util.List;
import java.util.UUID;

@Tag(name = "MateReply", description = "디저트메이트 댓글 관련 API")
@RestController
@RequestMapping("api/mates/{mateUuid}/reply")
@RequiredArgsConstructor
public class MateReplyController {

    private final MateReplyService mateReplyService;
    private final ObjectMapper objectMapper;

    /**
     * 디저트메이트 댓글 생성
     * */
    @Operation(summary = "메이트 댓글 생성", description = "디저트메이트를 댓글을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트메이트 댓글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<MateReplyResponse> createReply(@RequestPart("request")  String requestJson,
                                                         @PathVariable UUID mateUuid) {


        MateReplyCreateRequest request;


        try {

            request = objectMapper.readValue(requestJson, MateReplyCreateRequest.class);

        } catch (JsonProcessingException e) {

            throw new RuntimeException(e);
        }

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
                                                           @RequestParam int from,
                                                           @RequestParam int to) {


        return ResponseEntity.ok(mateReplyService.getReplies(mateUuid, from, to));
    }

    /**
     * 디저트메이트 댓글 수정
     * */
    @PatchMapping("/{replyId}")
    public ResponseEntity<String> updateMate(
            @PathVariable UUID mateUuid,
            @PathVariable Long replyId,
            @RequestPart(value = "request") String requestJson
    ){

        ObjectMapper objectMapper = new ObjectMapper();
        MateReplyCreateRequest request;

            //JSON 문자열을 MateCreateRequest 객체로 변환
        try {
            request = objectMapper.readValue(requestJson, MateReplyCreateRequest.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }

        mateReplyService.updateReply(mateUuid, replyId, request);
        return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");

    }

    /**
     * 디저트메이트 댓글 삭제
     * */
    @DeleteMapping("{replyId}")
    public ResponseEntity<String> deleteReply(@PathVariable UUID mateUuid, @PathVariable Long replyId) {

        mateReplyService.deleteReply(mateUuid, replyId);

        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }
}
