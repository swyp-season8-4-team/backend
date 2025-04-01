package org.swyp.dessertbee.community.mate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.MateUserIds;
import org.swyp.dessertbee.community.mate.dto.request.MateApplyMemberRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateApplyStatus;
import org.swyp.dessertbee.community.mate.entity.MateMember;
import org.swyp.dessertbee.community.mate.entity.MateMemberGrade;
import org.swyp.dessertbee.community.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
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


    /**
     * 디저트 메이트 생성 시 생성자 등록
     */
    @Override
    @Transactional
    public void addCreatorAsMember(UUID mateUuid, Long userId) {

        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_REPLY_NOT_FOUND));

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
                .map(mateMember -> userService.findById(mateMember.getUserId()))
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
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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

        Mate mate = mateRepository.findById(mateId).orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND));

        if(!mate.getRecruitYn()){
            throw new BusinessException(ErrorCode.MATE_RECRUIT_DONE);
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
            throw new BusinessException(ErrorCode.MATE_APPLY_BANNED);
        }

        if (mateMember.isPending()) {
            throw new BusinessException(ErrorCode.MATE_APPLY_WAIT);
        }

        if (mateMember.isReject()) {
            throw new BusinessException(ErrorCode.MATE_APPLY_REJECT);
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

        throw new BusinessException(ErrorCode.ALREADY_TEAM_MEMBER);



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
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_PENDING_MEMBER));


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

        MateUserIds validateMate = validateMate(mateUuid);
        Long mateId = validateMate.getMateId();

        List<MateMember> mateMembers = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndApplyStatus(validateMate.getMateId(), MateApplyStatus.PENDING);

        //userId로 userUuid 조회
        List<UserEntity> users = mateMembers.stream()
                .map(mateMember -> userService.findById(mateMember.getUserId()))
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
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
    public void acceptMember (UUID mateUuid, MateApplyMemberRequest request) {


        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid, request.getCreatorUserUuid());
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();

        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findGradeByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));
        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {

            UserEntity acceptUserEntity = userService.findByUserUuid(request.getAcceptUserUuid());

            Long acceptUserId = acceptUserEntity.getId();

            MateMember acceptUser = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, acceptUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));


            mateMemberRepository.updateApplyStatus(MateApplyStatus.APPROVED, mateId, acceptUser.getUserId());
        }


    }

    /**
     * 디저트 메이트 멤버 신청 거절 api
     * */
    @Override
    @Transactional
    public void rejectMember (UUID mateUuid, MateApplyMemberRequest request) {

        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid, request.getCreatorUserUuid());
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();

        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findGradeByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));

        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {


            try {

                UserEntity rejectUserEntity = userService.findByUserUuid(request.getRejectUserUuid());

                Long rejectUserId = rejectUserEntity.getId();

                MateMember rejectUser = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, rejectUserId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));


                mateMemberRepository.updateApplyStatus(MateApplyStatus.REJECTED, mateId, rejectUser.getUserId());
                rejectUser.updateStatus(MateApplyStatus.REJECTED);

                rejectUser.softDelete();

                // 변경된 모든 멤버 저장
                mateMemberRepository.save(rejectUser);
            } catch (BusinessException e){
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
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
    public void bannedMember (UUID mateUuid, MateApplyMemberRequest request) {

        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid, request.getCreatorUserUuid());
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();

        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findGradeByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));

        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {


            try {

                UserEntity banUserEntity = userService.findByUserUuid(request.getBanUserUuid());

                Long banUserId = banUserEntity.getId();

                MateMember banUser = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, banUserId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));

                mateMemberRepository.updateApplyStatus(MateApplyStatus.BANNED, mateId, banUser.getUserId());
                banUser.updateStatus(MateApplyStatus.BANNED);

                banUser.softDelete();

                // 변경된 모든 멤버 저장
                mateMemberRepository.save(banUser);

            }  catch (BusinessException e){
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            } catch (Exception e) {
                log.error("❌ 디저트메이트 멤버 강퇴 중 오류 발생: " + e.getMessage());
                throw new RuntimeException("디저트메이트 멤버 강퇴 실패: " + e.getMessage(), e);
            }
        } else {
            throw new BusinessException(ErrorCode.MATE_PERMISSION_DENIED);
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

        //디저트 메이트 멤버인지 확인
        mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));


        MateMember mateMember = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_MEMBER_NOT_FOUND));
        try {
            mateMember.softDelete();

            mateMemberRepository.save(mateMember);

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
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND));


        return new MateUserIds(mateId, null);
    }


    /**
     * User만 유효성 검사
     * */
    public MateUserIds validateUser (UUID userUuid){


        try {
            // userUuid로 userId 조회
            UserEntity user = userService.findByUserUuid(userUuid);
            Long userId = user.getId();

            return new MateUserIds(null, userId);

        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

    }



    /**
     * Mate와 User 한번에 유효성 검사
     * */
    private MateUserIds validateMateAndUser(UUID mateUuid, UUID userUuid) {

        // mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATE_NOT_FOUND));


        try {
            // userUuid로 userId 조회
            UserEntity user = userService.findByUserUuid(userUuid);
            Long userId = user.getId();


            return new MateUserIds(mateId, userId);

        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

    }


}

