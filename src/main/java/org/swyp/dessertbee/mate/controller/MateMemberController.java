package org.swyp.dessertbee.mate.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.mate.service.MateMemberService;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;
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
    public ResponseEntity<String> applyMate(@PathVariable UUID mateUuid, Long userId, UUID userUuid) {

        mateMemberService.applyMate(mateUuid, userId ,userUuid);
        return ResponseEntity.ok("디저트메이트에 성공적으로 신청되었습니다.");
    }

    /**
     * 디저트 메이트 멤버 신청 수락 api
     * */
    @PatchMapping("/apply")
    public ResponseEntity<String> acceptMemeber(@PathVariable UUID mateUuid, Long userId, UUID userUuid) {

        mateMemberService.acceptMember(mateUuid, userId, userUuid);
        return ResponseEntity.ok("팀원이 되었습니다~!");
    }

    /**
     * 디저트 메이트 멤버 신청 거절 api
     * */
    @DeleteMapping("/apply")
    public ResponseEntity<String> rejectMemeber(@PathVariable UUID mateUuid, UUID userUuid) {

        mateMemberService.rejectMember(mateUuid, userUuid);

        return ResponseEntity.ok("거절 되었습니다.");
    }

    /**
     * 디저트 메이트 멤버 강퇴 api
     * */
    @DeleteMapping("/members")
    public ResponseEntity<String> removeMember(@PathVariable UUID mateUuid, UUID creatorUuid, UUID targetUuid) {

        mateMemberService.removeMember(mateUuid, creatorUuid, targetUuid);

        return ResponseEntity.ok("성공적으로 강퇴 되었습니다.");
    }

    /**
     * 디저트 메이트 멤버 탈퇴 api
     * */
    @DeleteMapping("/leave")
    public ResponseEntity<String> leaveMember(@PathVariable UUID mateUuid, UUID userUuid) {

        mateMemberService.leaveMember(mateUuid, userUuid);
        return ResponseEntity.ok("성공적으로 탈퇴 되었습니다.");
    }
}
