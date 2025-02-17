package org.swyp.dessertbee.mate.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.mate.dto.MateUserIds;
import org.swyp.dessertbee.mate.dto.response.MateMemberResponse;
import org.swyp.dessertbee.mate.entity.MateMember;
import org.swyp.dessertbee.mate.entity.MateMemberGrade;
import org.swyp.dessertbee.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MateMemberService {


    private final MateMemberRepository mateMemberRepository;
    private final MateRepository mateRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;



    /**
     *
     * 디저트 메이트 생성 시 생성자 등록
     * */
    public void addCreatorAsMember(UUID mateUuid, Long userId) {

        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);

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
     * */
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



        }catch (Exception e){
            System.out.println("❌ 디저트메이트 멤버 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("디저트메이트 멤버 삭제 실패: " + e.getMessage(), e);
        }

    }


    /**
     * 디저트 메이트 멤버 전체 조회
     * */
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
                    List<String> userImages = imageService.getImagesByTypeAndId(ImageType.PROFILE, user.getId());

                    // MateMemberResponse 생성
                    return MateMemberResponse.fromEntity(mateMember, mateUuid, user, userImages);
                })
                .toList();
    }

    /**
     * 디저트메이트 신청 api
     * */
    public void applyMate(UUID mateUuid, UUID userUuid) throws BadRequestException {
        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, userUuid);
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        MateMember mateMember = mateMemberRepository.findByMateIdAndUserId(mateId, userId);

        if(mateMember != null) {
            throw new DuplicateApplyException("신청한 유저입니다. 신청 불가능합니다.");
        }

        //mateMember 테이블에 신청자 등록
        mateMemberRepository.save(
                MateMember.builder()
                        .mateId(mateId)
                        .userId(userId)
                        .grade(MateMemberGrade.NORMAL)
                        .approvalYn(false)
                        .build()
        );
    }

    /**
     * 디저트 메이트 멤버 신청 수락 api
     * */
    public void acceptMember(UUID mateUuid, UUID userUuid){
        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, userUuid);
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        mateMemberRepository.updateApprovalYn(mateId, userId);
    }

    /**
     * 디저트 메이트 멤버 신청 거절 api
     * */
    public void rejectMember(UUID mateUuid, UUID userUuid) {

        //mateId,userId  유효성 검사
        MateUserIds validate = validateMateAndUser(mateUuid, userUuid);
        Long mateId = validate.getMateId();
        Long userId = validate.getUserId();

        MateMember mateMember = mateMemberRepository.findByMateIdAndUserId(mateId, userId);
        if (mateMember == null) {
            throw new UserNotFoundExcption("디저트메이트 멤버로 존재하지 않는 유저입니다.");
        }

        try {
            mateMember.softDelete();

            // 변경된 모든 멤버 저장
            mateMemberRepository.save(mateMember);

        }catch (Exception e){
            System.out.println("❌ 디저트메이트 멤버 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("디저트메이트 멤버 삭제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Mate와 User 한번에 유효성 검사
     * */
    public MateUserIds validateMateAndUser(UUID mateUuid, UUID userUuid) {
        // mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);

        mateRepository.findByMateIdAndDeletedAtIsNull(mateId)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        // userUuid로 userId 조회
        Long userId = userRepository.findIdByUserUuid(userUuid);
        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }
        return new MateUserIds(mateId, userId);

    }

    /**
     * Mate만 유효성 검사
     * */
    public MateUserIds validateMate(UUID mateUuid) {



        // mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);



        mateRepository.findByMateIdAndDeletedAtIsNull(mateId)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        return new MateUserIds(mateId,null);
    }


    /**
     * User만 유효성 검사
     * */
    public MateUserIds validateUser(UUID userUuid) {

        // userUuid로 userId 조회
        Long userId = userRepository.findIdByUserUuid(userUuid);

        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }
        return new MateUserIds(null,userId);
    }

}
