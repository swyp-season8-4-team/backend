package org.swyp.dessertbee.mate.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.mate.dto.MateUserIds;
import org.swyp.dessertbee.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.mate.entity.MateMember;
import org.swyp.dessertbee.mate.entity.MateMemberGrade;
import org.swyp.dessertbee.mate.exception.MateExceptions;
import org.swyp.dessertbee.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MateMemberService {


    private final MateMemberRepository mateMemberRepository;
    private final MateRepository mateRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;


    /**
     * 디저트 메이트 생성 시 생성자 등록
     */
    @Transactional
    public void addCreatorAsMember(UUID mateUuid, Long userId) {

        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        //mateMember 테이블에 생성자 등록
        mateMemberRepository.save(
                MateMember.builder()
                        .mateId(mateId)
                        .userId(userId)
                        .grade(MateMemberGrade.CREATOR)
                        .approvalYn(true)
                        .build()
        );
    }

    /**
     * 디저트메이트 삭제 시 멤버 삭제
     */
    @Transactional
    public void deleteAllMember(Long mateId) {

        try {


            // mateId로 모든 멤버 조회
            List<MateMember> members = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndApprovalYnTrue(mateId);

            // 각 멤버에 대해 softDelete 처리
            for (MateMember member : members) {
                member.softDelete();  // 개별 멤버에 softDelete 적용
            }

            // 변경된 모든 멤버 저장
            mateMemberRepository.saveAll(members);


        } catch (Exception e) {
            System.out.println("❌ 디저트메이트 멤버 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("디저트메이트 멤버 삭제 실패: " + e.getMessage(), e);
        }

    }


    /**
     * 디저트 메이트 멤버 전체 조회
     */
    @Transactional
    public List<MateMemberResponse> getMembers(UUID mateUuid) {

        //mateId 유효성 검사
        MateUserIds validateMate = validateMate(mateUuid);

        List<MateMember> mateMembers = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndApprovalYnTrue(validateMate.getMateId());

        //userId로 userUuid 조회
        List<UserEntity> users = mateMembers.stream()
                .flatMap(mateMember ->
                        userRepository.findAllUserUuidAndNicknameById(mateMember.getUserId()).stream()
                )
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
                                .orElseThrow(() -> new UserNotFoundExcption("사용자 정보를 찾을 수 없습니다."));
                    } catch (UserNotFoundExcption e) {
                        throw new RuntimeException(e);
                    }

                    // 사용자별 프로필 이미지 조회
                    List<String> profileImages = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId());

                    // MateMemberResponse 생성
                    return MateMemberResponse.fromEntity(mateMember, mateUuid, user, profileImages);
                })
                .toList();
    }

    /**
     * 디저트메이트 신청 api
     *
     * @return
     */
    @Transactional
    public void applyMate(UUID mateUuid, UUID userUuid) {
        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, userUuid);
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        MateMember mateMember = mateMemberRepository.findByMateIdAndUserId(mateId, userId)
                .orElse(null);


        if(mateMember == null) {
            // mateMember가 없으면 새로 저장 후 종료
            mateMemberRepository.save(
                    MateMember.builder()
                            .mateId(mateId)
                            .userId(userId)
                            .grade(MateMemberGrade.NORMAL)
                            .approvalYn(false)
                            .bannedYn(false)
                            .build()
            );
            return;
        }

        if (mateMember.getBannedYn()) {
            throw new MateApplyBannedException("디저트메이트 강퇴 당한 사람입니다. 신청 불가능합니다.");
        }

        if (mateMember.isPending()) {
            throw new MateApplyWaitException("메이트 신청 대기 중입니다.");
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
                            .approvalYn(false)
                            .bannedYn(false)
                            .build()
            );
            return;  // ✅ 재신청이 끝나면 return으로 이후 예외 방지

        }

        throw new AlreadyTeamMemberException("해당 사용자는 이미 팀원입니다.");



    }


    /**
     * 디저트메이트 신청 취소 api
     * */
    public void cancelApplyMate(UUID mateUuid, UUID userUuid) {

        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, userUuid);
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        MateMember mateMember = mateMemberRepository.findByMateIdAndUserId(mateId, userId)
                .orElseThrow(() ->  new MateNotFoundException("디저트메이트 신청하신 분이 아닙니다."));

        assert mateMember != null;
        if(mateMember.isPending()){
            mateMemberRepository.delete(mateMember);
        }

    }
    /**
     * 디저트 메이트 대기 멤버 전체 조회
     **/
    public List<MateMemberResponse> pendingMate(UUID mateUuid) {

        MateUserIds validateMate = validateMate(mateUuid);
        Long mateId = validateMate.getMateId();

        List<MateMember> mateMembers = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndApprovalYnFalse(validateMate.getMateId());

        //userId로 userUuid 조회
        List<UserEntity> users = mateMembers.stream()
                .flatMap(mateMember ->
                        userRepository.findAllUserUuidAndNicknameById(mateMember.getUserId()).stream()
                )
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
                                .orElseThrow(() -> new UserNotFoundExcption("사용자 정보를 찾을 수 없습니다."));
                    } catch (UserNotFoundExcption e) {
                        throw new RuntimeException(e);
                    }

                    // 사용자별 프로필 이미지 조회
                    List<String> profileImages = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId());

                    // MateMemberResponse 생성
                    return MateMemberResponse.fromEntity(mateMember, mateUuid, user, profileImages);
                })
                .toList();
    }


    /**
     * 디저트 메이트 멤버 신청 수락 api
     * */
    @Transactional
    public void acceptMember (UUID mateUuid, UUID creatorUuid, UUID targetUuid){

        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid,creatorUuid);
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();

        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findGradeByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 멤버입니다."));

        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {

            Long targetId = userRepository.findIdByUserUuid(targetUuid);

            MateMember target = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, targetId)
                    .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 멤버입니다."));


            mateMemberRepository.updateApprovalYn(mateId, target.getUserId());
        }
    }

    /**
     * 디저트 메이트 멤버 신청 거절 api
     * */
    @Transactional
    public void rejectMember (UUID mateUuid, UUID creatorUuid, UUID targetUuid){

        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid,creatorUuid);
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();

        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findGradeByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 멤버입니다."));

        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {

            Long targetId = userRepository.findIdByUserUuid(targetUuid);

            MateMember target = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, targetId)
                    .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 멤버입니다."));

            try {
                target.softDelete();

                // 변경된 모든 멤버 저장
                mateMemberRepository.save(target);
            } catch (Exception e) {
                System.out.println("❌ 디저트메이트 멤버 삭제 중 오류 발생: " + e.getMessage());
                throw new RuntimeException("디저트메이트 멤버 삭제 실패: " + e.getMessage(), e);
            }
        }

    }


    /**
     * 디저트 메이트 멤버 강퇴 api
     * */
    @Transactional
    public void bannedMember (UUID mateUuid, UUID creatorUuid, UUID targetUuid){

        //생성자 권한을 위해 생성자 userId 조회 및 유효성 검사
        MateUserIds creatorIds  = validateMateAndUser(mateUuid,creatorUuid);
        Long mateId = creatorIds.getMateId();
        Long creatorId = creatorIds.getUserId();

        //mateMember 테이블에서 생성자 조회
        MateMember creator = mateMemberRepository.findGradeByMateIdAndUserIdAndDeletedAtIsNull(mateId, creatorId)
                .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 멤버입니다."));

        if (creator.getGrade().equals(MateMemberGrade.CREATOR)) {

            MateUserIds targetIds = validateMateAndUser(mateUuid, targetUuid);
            Long tragetId = targetIds.getUserId();

            MateMember target = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, tragetId)
                    .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 멤버입니다."));

            try {
                target.removeDelete();

                // 변경된 모든 멤버 저장
                mateMemberRepository.save(target);

            } catch (Exception e) {
                System.out.println("❌ 디저트메이트 멤버 강퇴 중 오류 발생: " + e.getMessage());
                throw new RuntimeException("디저트메이트 멤버 강퇴 실패: " + e.getMessage(), e);
            }
        } else {
            throw new PermissionDeniedException("메이트 관리자 권한이 없습니다.");
        }
    }



    /**
     * 디저트 메이트 멤버 탈퇴 api
     * */
    @Transactional
    public void leaveMember (UUID mateUuid, UUID userUuid){

        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, userUuid);
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        //디저트 메이트 멤버인지 확인
        mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, userId)
                .orElseThrow(() -> new MateMemberNotFoundExcption("디저트메이트 멤버가 아닙니다."));


        MateMember mateMember = mateMemberRepository.findByMateIdAndUserIdAndDeletedAtIsNull(mateId, userId)
                .orElseThrow(() -> new MateMemberNotFoundExcption("존재하지 않는 멤버입니다."));
        try {
            mateMember.softDelete();

            mateMemberRepository.save(mateMember);

        } catch (Exception e) {
            System.out.println("❌ 디저트메이트 멤버 탈퇴 중 오류 발생: " + e.getMessage());
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
     * User만 유효성 검사
     * */
    public MateUserIds validateUser (UUID userUuid){

        // userUuid로 userId 조회
        Long userId = userRepository.findIdByUserUuid(userUuid);

        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }
        return new MateUserIds(null, userId);
    }



    /**
     * Mate와 User 한번에 유효성 검사
     * */
    private MateUserIds validateMateAndUser(UUID mateUuid, UUID userUuid) {

        // mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        // userUuid로 userId 조회
        Long userId = userRepository.findIdByUserUuid(userUuid);
        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

        return new MateUserIds(mateId, userId);
    }


}

