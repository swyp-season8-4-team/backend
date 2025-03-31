package org.swyp.dessertbee.admin.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.admin.report.service.MateReportAdminService;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;

import java.util.List;

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

}
