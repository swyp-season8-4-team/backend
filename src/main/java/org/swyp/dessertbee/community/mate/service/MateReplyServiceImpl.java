package org.swyp.dessertbee.community.mate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.entity.ReportCategory;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.repository.ReportRepository;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.MateUserIds;
import org.swyp.dessertbee.community.mate.dto.request.MateReplyCreateRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateReportRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateReplyPageResponse;
import org.swyp.dessertbee.community.mate.dto.response.MateReplyResponse;
import org.swyp.dessertbee.community.mate.dto.response.MateReportResponse;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateReply;
import org.swyp.dessertbee.community.mate.entity.MateReport;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.community.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.community.mate.repository.MateReplyRepository;
import org.swyp.dessertbee.community.mate.repository.MateReportRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.user.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MateReplyServiceImpl implements MateReplyService {

    private final MateReplyRepository replyRepository;
    private final MateReplyRepository mateReplyRepository;
    private final MateMemberRepository mateMemberRepository;
    private final MateReportRepository mateReportRepository;
    private final ReportRepository reportRepository;
    private final MateRepository mateRepository;
    private final ImageService imageService;
    private final UserService userService;

    /**
     * 디저트메이트 댓글 생성
     * */
    @Override
    @Transactional
    public MateReplyResponse createReply(UUID mateUuid, MateReplyCreateRequest request) {


        //디저트 메이트 유효성 검사
        MateUserIds mateUserIds = validateMateAndUser(mateUuid, request.getUserUuid());
        Long mateId = mateUserIds.getMateId();
        Long userId = mateUserIds.getUserId();

        MateReply mateReply = replyRepository.save(
                MateReply.builder()
                        .mateId(mateId)
                        .userId(userId)
                        .content(request.getContent())
                .build()
        );

        return getReplyDetail(mateUuid, mateReply.getMateReplyId());
    }

    /**
     * 디저트메이트 댓글 조회(한개만)
     * */
    @Override
    public MateReplyResponse getReplyDetail(UUID mateUuid, Long replyId) {

        //디저트 메이트 유효성 검사
        validateMate(mateUuid);

        MateReply mateReply = mateReplyRepository.findById(replyId)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));

        try {
            UserEntity user = userService.findById(mateReply.getUserId());

            // 사용자별 프로필 이미지 조회
            String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, user.getId());

            return MateReplyResponse.fromEntity(mateReply, mateUuid, user, profileImage);
        }catch (BusinessException e) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }


    }

    /**
     * 디저트메이트 댓글 전체 조회
     * */
    @Override
    @Transactional
    public MateReplyPageResponse getReplies(UUID mateUuid, Pageable pageable) {
        MateUserIds mateUserIds = validateMate(mateUuid);
        Long mateId = mateUserIds.getMateId();

        // Pageable을 이용하여 데이터 조회
        Page<MateReply> repliesPage = mateReplyRepository.findAllByDeletedAtIsNull(mateId, pageable);

        // MateReplyResponse로 변환
        List<MateReplyResponse> repliesResponse = repliesPage.getContent()
                .stream()
                .map(mateReply -> getReplyDetail(mateUuid, mateReply.getMateReplyId()))
                .collect(Collectors.toList());

        // 다음 페이지 존재 여부
        boolean isLast = repliesPage.isLast();

        Long count = repliesPage.getTotalElements();

        return new MateReplyPageResponse(repliesResponse, isLast, count);
    }


    /**
     * 디저트메이트 댓글 수정
     * */
    @Override
    @Transactional
    public void updateReply(UUID mateUuid, Long replyId, MateReplyCreateRequest request) {

        validateMateAndUser(mateUuid, request.getUserUuid());

        //replyId 존재 여부 확인
        MateReply mateReply = mateReplyRepository.findById(replyId)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));

        try {
            userService.findById(mateReply.getUserId());

            mateReply.update(request.getContent());

        }catch (BusinessException e) {

            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }

    }


    /**
     * 디저트메이트 댓글 삭제
     * */
    @Override
    @Transactional
    public void deleteReply(UUID mateUuid, Long replyId) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        MateUserIds mateUserIds = validateMateAndUser(mateUuid, user.getUserUuid());
        Long mateId = mateUserIds.getMateId();

        MateReply mateReply = mateReplyRepository.findById(replyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_REPLY_NOT_FOUND));

        if(!mateReply.getUserId().equals(mateUserIds.getUserId())) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }

        try {

            mateReply.softDelete();

            mateReplyRepository.save(mateReply);

        } catch (Exception e) {

            log.error("❌ 디저트메이트 댓글 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("디저트메이트 댓글 삭제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 디저트메이트 댓글 신고
     * */
    public void reportMateReply(UUID mateUuid, Long replyId, MateReportRequest request) {
        MateUserIds mateUserIds = validateMateAndUser(mateUuid, request.getUserUuid());
        Long mateId = mateUserIds.getMateId();
        Long userId = mateUserIds.getUserId();

        MateReply mateReply = mateReplyRepository.findByMateIdAndMateReplyIdAndDeletedAtIsNull(mateId, replyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_REPLY_NOT_FOUND));


        //신고 유무 확인
        MateReport report = mateReportRepository.findByMateReplyIdAndUserId(replyId, userId);

        if(report != null){
            throw new DuplicationReportException("이미 신고된 게시물입니다.");
        }

        // 6L로 타입 일치
        // '기타' 신고인 경우 사용자가 입력한 코멘트를 그대로 저장
        if (request.getReportCategoryId().equals(6L)){
            mateReportRepository.save(
                    MateReport.builder()
                            .reportCategoryId(request.getReportCategoryId())
                            .mateReplyId(replyId)
                            .userId(userId)
                            .comment(request.getReportComment())
                            .build()
            );

            return;
        }

        //신고 유형 코멘트 조회
        ReportCategory reportCategory = reportRepository.findByReportCategoryId(request.getReportCategoryId());

        // '기타'가 아닌 경우 미리 정의된 신고 유형 코멘트 조회 후 저장
        mateReportRepository.save(
                MateReport.builder()
                        .reportCategoryId(request.getReportCategoryId())
                        .mateReplyId(replyId)
                        .userId(userId)
                        .comment(reportCategory.getReportComment())
                        .build()
        );


    }


    /**
     * Mate와 User 한번에 유효성 검사
     * */
    private MateUserIds validateMateAndUser(UUID mateUuid, UUID userUuid) {

        UserEntity user = userService.getCurrentUser();

        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND));

        // userUuid로 userId 조회
        Long userId = user.getId();

        try {
            userService.findById(userId);

            //디저트 메이트 멤버인지 확인
            mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mate.getMateId(), userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));


            return new MateUserIds(mate.getMateId(), userId);
        } catch (BusinessException e) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }



    }

    /**
     * Mate만 유효성 검사
     * */
    public MateUserIds validateMate (UUID mateUuid){


        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));


        return new MateUserIds(mate.getMateId(), null);
    }


//     -------------- 관리자용 메이트 댓글 신고 관리 기능 ------------


    /**
     *  신고된 Mate 댓글 조회
     */
    public List<MateReportResponse> getReportedMateReplies() {
        List<MateReport> reports = mateReportRepository.findAllByMateReplyIdIsNotNull();

        return reports.stream()
                .map(MateReportResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 신고된 Mate 댓글 삭제
     */
    @Transactional
    public void deleteReportedMateReply(Long mateReplyId) {
        boolean isReported = mateReportRepository.existsByMateReplyId(mateReplyId);
        if (!isReported) {
            throw new MateReplyNotRepotedException("신고되지 않은 디저트메이트 댓글입니다.");
        }

        MateReply mateReply = mateReplyRepository.findByMateReplyIdAndDeletedAtIsNull(mateReplyId)
                .orElseThrow(() ->  new MateReplyNotRepotedException("신고되지 않은 디저트메이트 댓글입니다."));

        mateReply.softDelete();
        mateReplyRepository.save(mateReply);
    }
}
