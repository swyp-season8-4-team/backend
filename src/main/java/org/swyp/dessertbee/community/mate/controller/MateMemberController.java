package org.swyp.dessertbee.community.mate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.community.mate.dto.request.MateAcceptRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateBannedRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateRejectRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.community.mate.service.MateMemberService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "MateMember", description = "디저트메이트 멤버 관련 API")
@RestController
@RequestMapping("api/mates/{mateUuid}")
@RequiredArgsConstructor
public class MateMemberController {

    private final MateMemberService mateMemberService;

    /**
     * 디저트 메이트 멤버 전체 조회
     * */
    @Operation(summary = "디저트 메이트 멤버 전체 조회(completed)", description = "디저트 메이트 멤버 전체 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "디저트 메이트 멤버 전체 조회 성공")
    })
    @ApiErrorResponses({ErrorCode.USER_NOT_FOUND})
    @GetMapping("/members")
    public ResponseEntity<List<MateMemberResponse>> getMembers(@PathVariable UUID mateUuid) {

        List<MateMemberResponse> members = mateMemberService.getMembers(mateUuid);

        return ResponseEntity.ok(members);
    }

    /**
     * 디저트 메이트 멤버 신청 api
     * */
    @Operation(summary = "디저트 메이트 멤버 신청(completed)", description = "디저트 메이트 멤버 신청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트 메이트 멤버 신청 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_RECRUIT_DONE, ErrorCode.MATE_APPLY_BANNED, ErrorCode.MATE_APPLY_WAIT, ErrorCode.MATE_APPLY_REJECT, ErrorCode.ALREADY_TEAM_MEMBER})
    @PostMapping("/apply")
        public ResponseEntity<Map<String, String>> applyMate(@PathVariable UUID mateUuid) {


        mateMemberService.applyMate(mateUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "디저트메이트에 성공적으로 신청되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 디저트메이트 멤버 신청 취소 api
     * */
    @Operation(summary = "디저트 메이트 멤버 신청 취소(completed)", description = "디저트 메이트 멤버 신청 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트 메이트 멤버 신청 취소 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_NOT_PENDING_MEMBER})
    @DeleteMapping("/apply")
    public ResponseEntity<Map<String, String>> cancelApplyMate(@PathVariable UUID mateUuid) {


        mateMemberService.cancelApplyMate(mateUuid);

        Map<String, String> response = new HashMap<>();
        response.put("message", "디저트메이트 성공적으로 신청 취소되었습니다.");
        return ResponseEntity.ok(response);
    }


    /**
     * 디저트 메이트 대기 멤버 전체 조회
     **/
    @Operation(summary = "디저트 메이트 대기 멤버 전체 조회(completed)", description = "디저트 메이트 대기 멤버 전체 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트 메이트 대기 멤버 전체 조회 성공"),
    })
    @ApiErrorResponses({ErrorCode.USER_NOT_FOUND})
    @GetMapping("/pending")
    public ResponseEntity<List<MateMemberResponse>> pendingMate(@PathVariable UUID mateUuid) {

        List<MateMemberResponse> members = mateMemberService.pendingMate(mateUuid);

        return ResponseEntity.ok(members);
    }


    /**
     * 디저트 메이트 멤버 신청 수락 api
     * */
    @Operation(summary = "디저트 메이트 멤버 신청 수락(completed)", description = "디저트 메이트 멤버 신청 수락합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트 메이트 멤버 신청 수락 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_MEMBER_NOT_FOUND})
    @PatchMapping("/apply")
    public ResponseEntity<Map<String, String>> acceptMember(@PathVariable UUID mateUuid, @RequestBody MateAcceptRequest request) {

        mateMemberService.acceptMember(mateUuid, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "팀원이 되었습니다~!");

        return ResponseEntity.ok(response);
    }

    /**
     * 디저트 메이트 멤버 신청 거절 api
     * */
    @Operation(summary = "디저트 메이트 멤버 신청 거절(completed)", description = "디저트 메이트 멤버 신청 거절합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트 메이트 멤버 신청 거절 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_MEMBER_NOT_FOUND, ErrorCode.USER_NOT_FOUND})
    @DeleteMapping("/reject")
    public ResponseEntity<Map<String, String>> rejectMember(@PathVariable UUID mateUuid, @RequestBody MateRejectRequest request) {

        mateMemberService.rejectMember(mateUuid, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "거절 되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 디저트 메이트 멤버 강퇴 api
     * */
    @Operation(summary = "디저트 메이트 멤버 신청 강퇴(completed)", description = "디저트 메이트 멤버 신청 강퇴합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트 메이트 멤버 신청 강퇴 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_MEMBER_NOT_FOUND, ErrorCode.USER_NOT_FOUND, ErrorCode.MATE_PERMISSION_DENIED})
    @DeleteMapping("/members")
    public ResponseEntity<Map<String, String>> bannedMember(@PathVariable UUID mateUuid, @RequestBody MateBannedRequest request) {

        mateMemberService.bannedMember(mateUuid, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 강퇴 되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 디저트 메이트 멤버 탈퇴 api
     * */
    @Operation(summary = "디저트 메이트 멤버 신청 탈퇴(completed)", description = "디저트 메이트 멤버 신청 탈퇴합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "디저트 메이트 멤버 신청 탈퇴 성공")
    })
    @ApiErrorResponses({ErrorCode.MATE_MEMBER_NOT_FOUND})
    @DeleteMapping("/leave")
    public ResponseEntity<Map<String, String>> leaveMember(@PathVariable UUID mateUuid) {

        mateMemberService.leaveMember(mateUuid);
        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 탈퇴 되었습니다.");
        return ResponseEntity.ok(response);
    }
}
