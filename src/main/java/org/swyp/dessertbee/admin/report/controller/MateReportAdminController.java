package org.swyp.dessertbee.admin.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.admin.report.service.MateReportAdminServiceImpl;
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

    private final MateReportAdminServiceImpl mateReportAdminServiceImpl;

    // 신고된 게시글 조회 API
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/report")
    public ResponseEntity<List<MateReportResponse>> getReportedMates() {
        List<MateReportResponse> reports = mateReportAdminServiceImpl.getReportedMates();
        return ResponseEntity.ok(reports);
    }

    // 게시글 삭제 API
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{mateUuid}/report")
    public ResponseEntity<Map<String, String>> deleteMate(@PathVariable UUID mateUuid) {
        mateReportAdminServiceImpl.deleteMateByUuid(mateUuid);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 신고된 게시글 댓글 조회 API
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/replies/report")
    public ResponseEntity<List<MateReportResponse>> getReportedMateReplies() {
        List<MateReportResponse> reportedReplies = mateReportAdminServiceImpl.getReportedMateReplies();
        return ResponseEntity.ok(reportedReplies);
    }

    // 신고된 Mate 댓글 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/replies/{mateReplyId}/report")
    public ResponseEntity<String> deleteReportedMateReply(@PathVariable Long mateReplyId) {
        mateReportAdminServiceImpl.deleteReportedMateReply(mateReplyId);
        return ResponseEntity.ok("신고된 댓글이 삭제되었습니다.");
    }



}
