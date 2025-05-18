package org.swyp.dessertbee.community.mate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.aop.CheckWriteRestriction;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.entity.ReportCategory;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.repository.ReportRepository;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.MateUserIds;
import org.swyp.dessertbee.community.mate.dto.request.MateAppReplyCreateRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateReplyCreateRequest;
import org.swyp.dessertbee.common.dto.ReportRequest;
import org.swyp.dessertbee.community.mate.dto.response.*;
import org.swyp.dessertbee.community.mate.entity.*;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.community.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.community.mate.repository.MateReplyRepository;
import org.swyp.dessertbee.community.mate.repository.MateReportRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.user.service.UserBlockService;
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
    private final UserBlockService userBlockService;

    /**
     * 디저트메이트 댓글 생성
     * */
    @Override
    @Transactional
    @CheckWriteRestriction
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
     * 디저트메이트 댓글 생성(앱)
     * */
    @Override
    @Transactional
    @CheckWriteRestriction
    public MateReplyResponse createAppReply(UUID mateUuid, MateAppReplyCreateRequest request){


        //디저트 메이트 유효성 검사
        MateUserIds mateUserIds = validateMateAndUser(mateUuid, request.getUserUuid());
        Long mateId = mateUserIds.getMateId();
        Long userId = mateUserIds.getUserId();

        if (request.getParentMateReplyId() != null) {
            MateReply parentReply = mateReplyRepository.findById(request.getParentMateReplyId())
                    .orElseThrow(() -> new MateReplyNotFoundException("부모 댓글이 존재하지 않습니다."));

            // 부모 댓글이 이미 대댓글이면 예외
            if (parentReply.getParentMateReplyId() != null) {
                throw new InvalidReplyDepthException("대댓글에는 댓글을 달 수 없습니다.");
            }
        }

            MateReply mateReply = replyRepository.save(
                    MateReply.builder()
                            .mateId(mateId)
                            .userId(userId)
                            .content(request.getContent())
                            .parentMateReplyId(request.getParentMateReplyId())
                            .build()
            );


            return getReplyDetail(mateUuid, mateReply.getMateReplyId());

    }
    /**
     * 디저트메이트 댓글 조회(한개만)
     * */
    @Override
    public MateReplyResponse getReplyDetail(UUID mateUuid, Long replyId) {
        UserEntity currentUser = userService.getCurrentUser();



        //디저트 메이트 유효성 검사
        validateMate(mateUuid);

        MateReply mateReply = mateReplyRepository.findByMateReplyIdAndDeletedAtIsNull(replyId)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));

        try {
            UserEntity replyUser = userService.findById(mateReply.getUserId());

            boolean blockedByAuthorYn = userBlockService.isBlocked(currentUser.getUserUuid(), replyUser.getUserUuid());

            // 사용자별 프로필 이미지 조회
            String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, replyUser.getId());

            return MateReplyResponse.fromEntity(mateReply, mateUuid, replyUser, profileImage, blockedByAuthorYn);
        }catch (BusinessException e) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }


    }

    /**
     * 디저트메이트 댓글 조회(한개만)(앱)
     * */
    @Override
    public MateAppReplyResponse getAppReplyDetail(UUID mateUuid, Long mateReplyId) {
        UserEntity currentUser = userService.getCurrentUser();

        MateReply reply = mateReplyRepository.findByMateReplyId(mateReplyId)
                .orElseThrow(() -> new MateReplyNotFoundException("댓글이 존재하지 않습니다."));

        UserEntity replyUser = userService.findById(reply.getUserId());


        // 사용자별 프로필 이미지 조회
        String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, replyUser.getId());
        boolean blockedByAuthorYn = userBlockService.isBlocked(currentUser.getUserUuid(), replyUser.getUserUuid());

        MateMemberGrade mateMemberGrade = mateMemberRepository.findGradeByMateIdAndUserIdAndDeletedAtIsNull(reply.getMateId(), replyUser.getId());
        // 자식 대댓글 조회
        List<MateReply> childReplies = mateReplyRepository.findByParentMateReplyId(reply.getMateReplyId());

        List<MateAppReplyResponse> children = childReplies.stream()
                .map(child -> getAppReplyDetail(mateUuid, child.getMateReplyId()))
                .collect(Collectors.toList());

        return MateAppReplyResponse.fromEntity(reply, replyUser, profileImage, children, mateMemberGrade, blockedByAuthorYn);
    }


    /**
     * 디저트메이트 댓글 전체 조회
     * */
    @Override
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
     * 디저트메이트 댓글 전체 조회(앱)
     * */
    @Override
    public MateAppReplyPageResponse getAppReplies(UUID mateUuid, Pageable pageable){

        MateUserIds mateUserIds = validateMate(mateUuid);
        Long mateId = mateUserIds.getMateId();


        Page<MateReply> repliesPage = mateReplyRepository.findAllByMateId(mateId, pageable);


        // 최상위 댓글만 필터링 (parentMateReplyId == null)
        List<MateAppReplyResponse> repliesResponse = repliesPage.getContent()
                .stream()
                .filter(reply -> reply.getParentMateReplyId() == null)
                .map(reply -> getAppReplyDetail(mateUuid, reply.getMateReplyId()))
                .collect(Collectors.toList());

        boolean isLast = repliesPage.isLast();
        Long count = repliesPage.getTotalElements();

        return new MateAppReplyPageResponse(mateUuid, repliesResponse, isLast, count);
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
    @Override
    @Transactional
    public void reportMateReply(UUID mateUuid, Long replyId, ReportRequest request) {
        MateUserIds mateUserIds = validateMateAndUser(mateUuid, request.getUserUuid());
        Long mateId = mateUserIds.getMateId();
        Long userId = mateUserIds.getUserId();

        mateReplyRepository.findByMateIdAndMateReplyIdAndDeletedAtIsNull(mateId, replyId)
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

        //디저트 메이트 멤버인지 확인
        mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mate.getMateId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));

        try {
            userService.findById(userId);


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

}
