package org.swyp.dessertbee.mate.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.mate.service.MateMemberService;

import java.util.List;
import java.util.UUID;

@Tag(name = "MateMember", description = "디저트메이트 멤버 관련 API")
@RestController
@RequestMapping("api/mates/{mateUuid}")
@RequiredArgsConstructor
public class MateMemberController {

    private final MateMemberService memberService;

    /**
     * 디저트 메이트 멤버 전체 조회
     * */
    @GetMapping("/members")
    public ResponseEntity<List<MateMemberResponse>> getMemberList(@PathVariable UUID mateUuid) {

        return ResponseEntity.ok(memberService.getMemberList(mateUuid));
    }



}
