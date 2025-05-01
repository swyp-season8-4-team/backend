package org.swyp.dessertbee.admin.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.admin.report.dto.response.MateReplyReportCountResponse;
import org.swyp.dessertbee.admin.report.dto.response.MateReportCountResponse;
import org.swyp.dessertbee.admin.report.dto.response.ReportActionResponse;
import org.swyp.dessertbee.admin.report.service.MateReportAdminService;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "AdminMateReport", description = "관리자용 디저트 메이트 신고 관리 API")
@RestController
@RequestMapping("/api/admin/mates")
@RequiredArgsConstructor
public class MateReportAdminController {

    private final MateReportAdminService mateReportAdminService;

    // 신고된 게시글 조회 API
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/report")
    public ResponseEntity<List<MateReportResponse>> getReportedMates() {
        List<MateReportResponse> reports = mateReportAdminService.getReportedMates();
        return ResponseEntity.ok(reports);
    }

    //신고된 게시글의 신고 횟수 조회 API
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/report/{mateUuid}/count")
    public ResponseEntity<MateReportCountResponse> getMateReportCount(@PathVariable UUID mateUuid) {
        MateReportCountResponse response = mateReportAdminService.getMateReportCount(mateUuid);
        return ResponseEntity.ok(response);
    }


    // 신고된 게시글 삭제 API (1단계)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/report/{mateUuid}")
    public ResponseEntity<Map<String, String>> deleteMate(@PathVariable UUID mateUuid) {
        mateReportAdminService.deleteMateByUuid(mateUuid);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }


    // 신고된 게시글 댓글 조회 API
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/replies/report")
    public ResponseEntity<List<MateReportResponse>> getReportedMateReplies() {
        List<MateReportResponse> reportedReplies = mateReportAdminService.getReportedMateReplies();
        return ResponseEntity.ok(reportedReplies);
    }

    //신고된 댓글의 신고 횟수 조회 API
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/replies/report/{mateReplyId}/count")
    public ResponseEntity<MateReplyReportCountResponse> getMateReplyReportCount(@PathVariable Long mateReplyId) {
        MateReplyReportCountResponse response = mateReportAdminService.getMateReplyReportCount(mateReplyId);
        return ResponseEntity.ok(response);
    }

    // 신고된 Mate 댓글 삭제 (1단계)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/replies/report/{mateReplyId}")
    public ResponseEntity<String> deleteReportedMateReply(@PathVariable Long mateReplyId) {
        mateReportAdminService.deleteReportedMateReply(mateReplyId);
        return ResponseEntity.ok("신고된 댓글이 삭제되었습니다.");
    }
    // -------------------- 신고 조치(2, 3단계) 통합 API --------------------

    // 2단계 경고 (동일 유형 3회 이상)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/report/action/warn")
    public ResponseEntity<ReportActionResponse> warnAuthor(
            @RequestParam(required = false) UUID mateUuid,
            @RequestParam(required = false) Long mateReplyId,
            @RequestParam Long reportCategoryId
    ) {
        ReportActionResponse response = mateReportAdminService.warnAuthor(mateUuid, mateReplyId, reportCategoryId);
        return ResponseEntity.ok(response);
    }

    // 3단계 계정 정지 (한 달)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/report/action/suspend")
    public ResponseEntity<ReportActionResponse> suspendAuthor(
            @RequestParam(required = false) UUID mateUuid,
            @RequestParam(required = false) Long mateReplyId
    ) {
        ReportActionResponse response = mateReportAdminService.suspendAuthor(mateUuid, mateReplyId);
        return ResponseEntity.ok(response);
    }

    // 3단계 작성 제한 (7일)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/report/action/restrict-writing")
    public ResponseEntity<ReportActionResponse> restrictAuthorWriting(
            @RequestParam(required = false) UUID mateUuid,
            @RequestParam(required = false) Long mateReplyId
    ) {
        ReportActionResponse response = mateReportAdminService.restrictAuthorWriting(mateUuid, mateReplyId);
        return ResponseEntity.ok(response);
    }

}
