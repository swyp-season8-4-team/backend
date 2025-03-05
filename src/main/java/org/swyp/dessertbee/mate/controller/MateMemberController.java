package org.swyp.dessertbee.mate.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.mate.dto.request.MateApplyMemberRequest;
import org.swyp.dessertbee.mate.dto.request.MateRequest;
import org.swyp.dessertbee.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.mate.service.MateMemberService;
import org.swyp.dessertbee.mate.service.MateService;

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
    @GetMapping("/members")
    public ResponseEntity<List<MateMemberResponse>> getMembers(@PathVariable UUID mateUuid) {

        List<MateMemberResponse> members = mateMemberService.getMembers(mateUuid);

        return ResponseEntity.ok(members);
    }

    /**
     * 디저트 메이트 멤버 신청 api
     * */
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
    @GetMapping("/pending")
    public ResponseEntity<List<MateMemberResponse>> pendingMate(@PathVariable UUID mateUuid) {

        List<MateMemberResponse> members = mateMemberService.pendingMate(mateUuid);

        return ResponseEntity.ok(members);
    }
    /**
     * 디저트 메이트 멤버 신청 수락 api
     * */
    @PatchMapping("/apply")
    public ResponseEntity<Map<String, String>> acceptMemeber(@PathVariable UUID mateUuid, @RequestBody MateApplyMemberRequest request) {

        mateMemberService.acceptMember(mateUuid, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "팀원이 되었습니다~!");

        return ResponseEntity.ok(response);
    }

    /**
     * 디저트 메이트 멤버 신청 거절 api
     * */
    @DeleteMapping("/reject")
    public ResponseEntity<Map<String, String>> rejectMemeber(@PathVariable UUID mateUuid, @RequestBody MateApplyMemberRequest request) {

        mateMemberService.rejectMember(mateUuid, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "거절 되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 디저트 메이트 멤버 강퇴 api
     * */
    @DeleteMapping("/members")
    public ResponseEntity<Map<String, String>> bannedMember(@PathVariable UUID mateUuid, @RequestBody MateApplyMemberRequest request) {

        mateMemberService.bannedMember(mateUuid, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 강퇴 되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 디저트 메이트 멤버 탈퇴 api
     * */
    @DeleteMapping("/leave")
    public ResponseEntity<Map<String, String>> leaveMember(@PathVariable UUID mateUuid) {

        mateMemberService.leaveMember(mateUuid);
        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 탈퇴 되었습니다.");
        return ResponseEntity.ok(response);
    }
}
