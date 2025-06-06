package org.swyp.dessertbee.community.mate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.MateUserIds;
import org.swyp.dessertbee.community.mate.dto.request.MateAcceptRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateBannedRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateRejectRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateApplyStatus;
import org.swyp.dessertbee.community.mate.entity.MateMember;
import org.swyp.dessertbee.community.mate.entity.MateMemberGrade;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.community.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.user.service.UserBlockService;
import org.swyp.dessertbee.user.service.UserService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MateMemberServiceImpl implements MateMemberService {


    private final MateMemberRepository mateMemberRepository;
    private final MateRepository mateRepository;
    private final ImageService imageService;
    private final UserService userService;
    private final UserBlockService userBlockService;


    /**
     * 디저트 메이트 생성 시 생성자 등록
     */
    @Override
    @Transactional
    public void addCreatorAsMember(UUID mateUuid, Long userId) {

        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new MateReplyNotFoundException("존재하지 않는 댓글입니다."));

        //mateMember 테이블에 생성자 등록
        mateMemberRepository.save(
                MateMember.builder()
                        .mateId(mateId)
                        .userId(userId)
                        .grade(MateMemberGrade.CREATOR)
                        .applyStatus(MateApplyStatus.APPROVED)
                        .build()
        );
    }

    /**
     * 디저트메이트 삭제 시 멤버 삭제
     */
    @Override
    @Transactional
    public void deleteAllMember(Long mateId) {

        try {


            // mateId로 모든 멤버 조회
            List<MateMember> members = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndApplyStatus(mateId, MateApplyStatus.APPROVED);

            // 각 멤버에 대해 softDelete 처리
            for (MateMember member : members) {
                member.softDelete();  // 개별 멤버에 softDelete 적용
            }

            // 변경된 모든 멤버 저장
            mateMemberRepository.saveAll(members);


        } catch (Exception e) {
            log.error("❌ 디저트메이트 멤버 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("디저트메이트 멤버 삭제 실패: " + e.getMessage(), e);
        }

    }


    /**
     * 디저트 메이트 멤버 전체 조회
     */
    @Override
    @Transactional
    public List<MateMemberResponse> getMembers(UUID mateUuid) {

        //mateId 유효성 검사
        MateUserIds validateMate = validateMate(mateUuid);

        List<MateMember> mateMembers = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndApplyStatusAndGrade_Normal(validateMate.getMateId(), MateApplyStatus.APPROVED, MateMemberGrade.NORMAL);

        //userId로 userUuid 조회
        List<UserEntity> users = mateMembers.stream()
                .map(mateMember -> userService.findByIdAndDeletedAtIsNull(mateMember.getUserId()))
                .toList();


        // MateMember와 UserEntity를 매칭하여 MateMemberResponse 생성
        return mateMembers.stream()
                .map(mateMember -> {
                    // 사용자 정보 찾기
                    UserEntity user = null;
                    try {
                       user = users.stream()
                                .filter(u -> u.getId().equals(mateMember.getUserId()))
                                .findFirst()
                                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
                    } catch (BusinessException e) {
                        throw new RuntimeException(e);
                    }

                    // 사용자별 프로필 이미지 조회
                    String profileImages = imageService.getImageByTypeAndId(ImageType.PROFILE, user.getId());

                    // MateMemberResponse 생성
                    return MateMemberResponse.fromEntity(mateMember, user, profileImages);
                })
                .toList();
    }

    /**
     * 디저트메이트 신청 api
     */
    @Override
    @Transactional
    public void applyMate(UUID mateUuid) {
        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, user.getUserUuid());
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        Mate mate = mateRepository.findById(mateId).orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        if(!mate.getRecruitYn()){
            throw new MateRecruitDoneException("해당 디저트메이트 모집 마감입니다.");
        }

        MateMember mateMember = mateMemberRepository.findByMateIdAndUserId(mateId, userId)
                .orElse(null);


        if(mateMember == null) {
            // mateMember가 없으면 새로 저장 후 종료
            mateMemberRepository.save(
                    MateMember.builder()
                            .mateId(mateId)
                            .userId(userId)
                            .grade(MateMemberGrade.NORMAL)
                            .applyStatus(MateApplyStatus.PENDING)
                            .build()
            );
            return;
        }

        if (mateMember.isBanned()) {
            throw new MateApplyBannedException("디저트메이트 강퇴 당한 사람입니다. 신청 불가능합니다.");
        }

        if (mateMember.isPending()) {
            throw new MatePendingException("메이트 신청 대기 중입니다.");
        }

        if (mateMember.isReject()) {
            throw new MateApplyRejectException("거절 된 메이트입니다. 신청 불가능합니다.");
        }




        //재신청 로직
        if (mateMember.isReapply()) {

            mateMemberRepository.delete(mateMember);
            mateMemberRepository.save(
                    MateMember.builder()
                            .mateId(mateId)
                            .userId(userId)
                            .grade(MateMemberGrade.NORMAL)
                            .applyStatus(MateApplyStatus.PENDING)
                            .build()
            );
            return;  // ✅ 재신청이 끝나면 return으로 이후 예외 방지

        }

        throw new AlreadyTeamMemberException("해당 사용자는 이미 팀원입니다.");



    }


    /**
     * 디저트메이트 신청 취소 api
     * */
    @Override
    public void cancelApplyMate(UUID mateUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, user.getUserUuid());
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        MateMember mateMember = mateMemberRepository.findByMateIdAndUserId(mateId, userId)
                .orElseThrow(() -> new MateNotPendingException("디저트메이트 신청하신 분이 아닙니다."));


        assert mateMember != null;
        if(mateMember.isPending()){
            mateMemberRepository.delete(mateMember);
        }

    }

    /**
     * 디저트 메이트 대기 멤버 전체 조회
     **/
    @Override
    public List<MateMemberResponse> pendingMate(UUID mateUuid) {
        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity currentUser = userService.getCurrentUser();

        MateUserIds validateMate = validateMate(mateUuid);

        List<Long> blockedUserIds = userBlockService.getBlockedUserIds(currentUser.getUserUuid());

        List<MateMember> mateMembers;
        if (blockedUserIds.isEmpty()) {
            mateMembers = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndApplyStatus(
                    validateMate.getMateId(),
                    MateApplyStatus.PENDING
            );
        } else {
            mateMembers = mateMemberRepository
                    .findByMateIdAndDeletedAtIsNullAndApplyStatusAndUserIdNotIn(
                            validateMate.getMateId(),
                            MateApplyStatus.PENDING,
                            blockedUserIds
                    );
        }
        //userId로 userUuid 조회
        List<UserEntity> users = mateMembers.stream()
                .map(mateMember -> userService.findByIdAndDeletedAtIsNull(mateMember.getUserId()))
                .toList();


        // MateMember와 UserEntity를 매칭하여 MateMemberResponse 생성
        return mateMembers.stream()
                .map(mateMember -> {
                    // 사용자 정보 찾기
                    UserEntity user = null;
                    try {
                        user = users.stream()
                                .filter(u -> u.getId().equals(mateMember.getUserId()))
                                .findFirst()
                                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
                    } catch (BusinessException e) {
                        throw new RuntimeException(e);
                    }

                    // 사용자별 프로필 이미지 조회
                   String profileImages = imageService.getImageByTypeAndId(ImageType.PROFILE, user.getId());

                    // MateMemberResponse 생성
                    return MateMemberResponse.fromEntity(mateMember, user, profileImages);
                })
                .toList();
    }


    /**
     * 디저트 메이트 멤버 신청 수락 api
     * */
    @Override
    @Transactional
    public void acceptMember (UUID mateUuid, MateAcceptRequest request) {


        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid, request.getCreatorUserUuid());
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();

        Mate mate = mateRepository.findById(mateId).orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));
        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {

            UserEntity acceptUserEntity = userService.findByUserUuid(request.getAcceptUserUuid());

            Long acceptUserId = acceptUserEntity.getId();

            MateMember acceptUser = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, acceptUserId)
                    .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));


            if (mate.getCurrentMemberCount() + 1 > mate.getCapacity()) {
                throw new MateCapacityExceededException("최대 수용 인원 초과입니다.");
            }

            // capacity와 딱 맞아지면 모집 마감 처리
            if ((mate.getCurrentMemberCount() +1 ) == mate.getCapacity()) {
                mate.updateRecruitYn(false);
            }

            // 모집 인원 추가 처리
            mate.updateCurrentMemberCount(mate.getCurrentMemberCount() + 1);
            mateRepository.save(mate);

            mateMemberRepository.updateApplyStatus(MateApplyStatus.APPROVED, mateId, acceptUser.getUserId());

        }


    }

    /**
     * 디저트 메이트 멤버 신청 거절 api
     * */
    @Override
    @Transactional
    public void rejectMember (UUID mateUuid, MateRejectRequest request) {

        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid, request.getCreatorUserUuid());
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();

        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));

        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {


            try {

                UserEntity rejectUserEntity = userService.findByUserUuid(request.getRejectUserUuid());

                Long rejectUserId = rejectUserEntity.getId();

                MateMember rejectUser = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, rejectUserId)
                        .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));


                mateMemberRepository.updateApplyStatus(MateApplyStatus.REJECTED, mateId, rejectUser.getUserId());
                rejectUser.updateStatus(MateApplyStatus.REJECTED);

                rejectUser.softDelete();

                // 변경된 모든 멤버 저장
                mateMemberRepository.save(rejectUser);
            } catch (BusinessException e){
                throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
            }catch (Exception e) {
                log.error("❌ 디저트메이트 멤버 삭제 중 오류 발생: " + e.getMessage());
                throw new RuntimeException("디저트메이트 멤버 삭제 실패: " + e.getMessage(), e);
            }
        }

    }


    /**
     * 디저트 메이트 멤버 강퇴 api
     * */
    @Override
    @Transactional
    public void bannedMember (UUID mateUuid, MateBannedRequest request) {

        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid, request.getCreatorUserUuid());
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();


        Mate mate = mateRepository.findById(mateId).orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));

        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {


            try {

                UserEntity banUserEntity = userService.findByUserUuid(request.getBanUserUuid());

                Long banUserId = banUserEntity.getId();

                MateMember banUser = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, banUserId)
                        .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));

                mateMemberRepository.updateApplyStatus(MateApplyStatus.BANNED, mateId, banUser.getUserId());
                banUser.updateStatus(MateApplyStatus.BANNED);

                banUser.softDelete();

                // 변경된 모든 멤버 저장
                mateMemberRepository.save(banUser);

                Long currentMemberCount = mate.getCurrentMemberCount() -1;
                // 모집 인원 삭제 처리
                mate.updateCurrentMemberCount(currentMemberCount);

                // 현재 인원이 정원보다 작아지면 다시 모집 가능하게
                if (currentMemberCount < mate.getCapacity()) {
                    mate.updateRecruitYn(true);
                }

                mateRepository.save(mate);

            }  catch (BusinessException e){
                throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
            } catch (Exception e) {
                log.error("❌ 디저트메이트 멤버 강퇴 중 오류 발생: " + e.getMessage());
                throw new RuntimeException("디저트메이트 멤버 강퇴 실패: " + e.getMessage(), e);
            }
        } else {
            throw new PermissionDeniedException("메이트 관리자 권한이 없습니다.");
        }
    }



    /**
     * 디저트 메이트 멤버 탈퇴 api
     * */
    @Override
    @Transactional
    public void leaveMember (UUID mateUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();


        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, user.getUserUuid());
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        Mate mate = mateRepository.findById(mateId).orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        try {

            //디저트 메이트 멤버인지 확인
            mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, userId)
                    .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));


            MateMember leaveMember = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, userId)
                    .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));

            leaveMember.softDelete();

            mateMemberRepository.save(leaveMember);

            Long currentMemberCount = mate.getCurrentMemberCount() -1;
            // 모집 인원 삭제 처리
            mate.updateCurrentMemberCount(currentMemberCount);

            // 현재 인원이 정원보다 작아지면 다시 모집 가능하게
            if (currentMemberCount < mate.getCapacity()) {
                mate.updateRecruitYn(true);
            }

        } catch (Exception e) {
            log.error("❌ 디저트메이트 멤버 탈퇴 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("디저트메이트 멤버 탈퇴 실패: " + e.getMessage(), e);

        }

    }

    /**
     * Mate만 유효성 검사
     * */
    public MateUserIds validateMate (UUID mateUuid){


        // mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        return new MateUserIds(mateId, null);
    }



    /**
     * Mate와 User 한번에 유효성 검사
     * */
    private MateUserIds validateMateAndUser(UUID mateUuid, UUID userUuid) {

        // mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        try {
            // userUuid로 userId 조회
            UserEntity user = userService.findByUserUuid(userUuid);
            Long userId = user.getId();


            return new MateUserIds(mateId, userId);

        } catch (BusinessException e) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }

    }


}

