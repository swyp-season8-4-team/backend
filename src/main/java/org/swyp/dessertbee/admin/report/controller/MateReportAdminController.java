package org.swyp.dessertbee.admin.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.admin.report.dto.response.MateReplyReportCountResponse;
import org.swyp.dessertbee.admin.report.dto.response.MateReportCountResponse;
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

    // 신고된 Mate 게시글 사용자 경고 (2단계)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/report/{mateUuid}/warn")
    public ResponseEntity<Void> warnMateAuthor(
            @PathVariable UUID mateUuid,
            @RequestParam Long reportCategoryId
    ) {
        mateReportAdminService.warnMateAuthor(mateUuid, reportCategoryId);
        return ResponseEntity.ok().build();
    }

    //신고된 게시글 작성자 정지 (3단계)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/report/{mateUuid}/suspend")
    public ResponseEntity<Void> suspendMateAuthor(@PathVariable UUID mateUuid) {
        mateReportAdminService.suspendMateAuthor(mateUuid);
        return ResponseEntity.ok().build();
    }

    //----------------- 댓글 ------------------

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

    //신고된 Mate 댓글 작성자 경고 (2단계)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/replies/report/{mateReplyId}/warn")
    public ResponseEntity<Void> warnMateReplyAuthor(
            @PathVariable Long mateReplyId,
            @RequestParam Long reportCategoryId
    ) {
        mateReportAdminService.warnMateReplyAuthor(mateReplyId, reportCategoryId);
        return ResponseEntity.ok().build();
    }

    // 신고된 댓글 작성자 정지 (3단계)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/replies/report/{mateReplyId}/suspend")
    public ResponseEntity<Void> suspendMateReplyAuthor(@PathVariable Long mateReplyId) {
        mateReportAdminService.suspendMateReplyAuthor(mateReplyId);
        return ResponseEntity.ok().build();
    }

}
