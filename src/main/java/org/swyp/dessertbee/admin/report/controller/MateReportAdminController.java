package org.swyp.dessertbee.admin.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.admin.report.service.MateReportAdminService;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "AdminMateReport", description = "관리자용 디저트 메이트 신고 관리 API")
@RestController
@RequestMapping("api/admin/mates")
@RequiredArgsConstructor
public class MateReportAdminController {

    private final MateReportAdminService mateReportAdminService;

    // 신고된 게시글 조회 API
    @GetMapping("/report")
    public ResponseEntity<List<MateReportResponse>> getReportedMates() {
        List<MateReportResponse> reports = mateReportAdminService.getReportedMates();
        return ResponseEntity.ok(reports);
    }

    // 게시글 삭제 API
    @DeleteMapping("/{mateUuid}/report")
    public ResponseEntity<Map<String, String>> deleteMate(@PathVariable UUID mateUuid) {
        mateReportAdminService.deleteMateByUuid(mateUuid);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }


}
