package org.swyp.dessertbee.community.mate.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.community.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateReportRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;
import org.swyp.dessertbee.community.mate.dto.response.MatesPageResponse;

import java.util.List;
import java.util.UUID;

public interface MateService {

    /** 메이트 등록 */
    MateDetailResponse createMate(MateCreateRequest request, MultipartFile mateImage);

    /** 메이트 상세 정보 */
    MateDetailResponse getMateDetail(UUID mateUuid);

    /** 메이트 삭제 */
    void deleteMate(UUID mateUuid);

    /** 메이트 수정 */
    void updateMate(UUID mateUuid, MateCreateRequest request, MultipartFile mateImage);

    /** 디저트메이트 전체 조회 */
    MatesPageResponse getMates(Pageable pageable, String keyword, Long mateCategoryId);

    /** 내가 참여한 디저트메이트 조회 */
    MatesPageResponse getMyMates(Pageable pageable);

    /** 디저트메이트 신고*/
    void reportMate(UUID mateUuid, MateReportRequest request);

//    -------------- 관리자용 메이트 신고 관리 기능 ------------

    /** 신고된 Mate 게시글 목록 조회 */
    List<MateReportResponse> getReportedMates();

    /** 신고된 Mate 삭제 */
    void deleteMateByUuid(UUID mateUuid);
}
