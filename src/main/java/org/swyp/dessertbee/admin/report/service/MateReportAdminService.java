package org.swyp.dessertbee.admin.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;
import org.swyp.dessertbee.community.mate.entity.MateReport;
import org.swyp.dessertbee.community.mate.repository.MateReplyRepository;
import org.swyp.dessertbee.community.mate.repository.MateReportRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateReportAdminService {

    private final MateReportRepository mateReportRepository;
    private final MateRepository mateRepository;
    private final MateReplyRepository mateReplyRepository;

    // 신고된 Mate 게시글 목록 조회
    public List<MateReportResponse> getReportedMates() {
        List<MateReport> reports = mateReportRepository.findAllByMateIdIsNotNull();
        return reports.stream()
                .map(MateReportResponse::new)
                .collect(Collectors.toList());
    }

