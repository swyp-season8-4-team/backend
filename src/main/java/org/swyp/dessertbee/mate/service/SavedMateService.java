package org.swyp.dessertbee.mate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.mate.dto.request.MateRequest;
import org.swyp.dessertbee.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.mate.entity.Mate;
import org.swyp.dessertbee.mate.entity.MateApplyStatus;
import org.swyp.dessertbee.mate.entity.MateMember;
import org.swyp.dessertbee.mate.entity.SavedMate;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.mate.repository.MateCategoryRepository;
import org.swyp.dessertbee.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;
import org.swyp.dessertbee.mate.repository.SavedMateRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserService;
import org.swyp.dessertbee.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedMateService {

    private final MateRepository mateRepository;
    private final UserRepository userRepository;
    private final SavedMateRepository savedMateRepository;
    private final MateMemberRepository mateMemberRepository;
    private final MateCategoryRepository mateCategoryRepository;
    private final ImageService imageService;
    private final UserServiceImpl userServiceImpl;

    /**
     * 디저트메이트 저장
     * */
    @Transactional
    public void saveMate(UUID mateUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userServiceImpl.getCurrentUser();

        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        // userUuid로 userId 조회
        Long userId = userRepository.findIdByUserUuid(user.getUserUuid());
        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

        SavedMate savedMate = savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), userId);
        if(savedMate != null) {
            throw new DuplicationSavedMateException("이미 저장된 디저트메이트입니다.");
        }

        savedMateRepository.save(
                SavedMate.builder()
                        .mate(mate)
                        .userId(userId)
                        .build()
        );


    }

    /**
     * 디저트메이트 삭제
     * */
    @Transactional
    public void deleteSavedMate(UUID mateUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userServiceImpl.getCurrentUser();

        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        // userUuid로 userId 조회
        Long userId = userRepository.findIdByUserUuid(user.getUserUuid());


        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

        SavedMate savedMate = savedMateRepository.findByMate_MateIdAndUserId(mateId, userId);
        if(savedMate == null) {
            throw new SavedMateNotFoundException("저장하지 않은 디저트메이트입니다.");
        }

        savedMateRepository.delete(savedMate);
    }

    /**
     * 저장된 디저트메이트 조회 (userId에 해당하는 Mate만 가져오기)
     */
    public MatesPageResponse getSavedMates(Pageable pageable) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userServiceImpl.getCurrentUser();

        Long userId = userRepository.findIdByUserUuid(user.getUserUuid());


        // 1️⃣ 현재 사용자가 저장한 SavedMate 목록을 가져오기 (페이징 처리)
        Page<SavedMate> savedMates = savedMateRepository.findByUserId(pageable, userId);


        // 2️⃣ 저장된 Mate ID 목록 추출
        List<Long> savedMateIds = savedMates.getContent().stream()
                .map(savedMate -> savedMate.getMate().getMateId()) // ✅ 저장된 Mate ID만 가져오기
                .collect(Collectors.toList());

        if (savedMateIds.isEmpty()) {
            return new MatesPageResponse(Collections.emptyList(), true); // ✅ 저장된 Mate가 없으면 빈 응답 반환
        }

        // 3️⃣ 저장된 Mate ID 목록을 사용하여 Mate 정보 가져오기
        List<MateDetailResponse> matesResponses = mateRepository.findByMateIdIn(savedMateIds)
                .stream()
                .map(mate -> {
                    List<String> mateImages = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());
                    String mateCategory = mateCategoryRepository.findCategoryNameById(mate.getMateCategoryId());
                    UserEntity creator = mateMemberRepository.findByMateId(mate.getMateId());
                    List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, mate.getUserId());

                    //저장된 디저트메이트 데이터만 지고 오는거니까 true
                    boolean saved = true;


                    //신청했는지 유무 확인
                    MateMember applyMember = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndUserId(mate.getMateId(), userId);

                    return MateDetailResponse.fromEntity(mate, mateImages, mateCategory, creator, profileImage, saved, applyMember.getApplyStatus());
                })
                .collect(Collectors.toList());

        // 4️⃣ 다음 페이지 존재 여부 확인
        boolean isLast = savedMates.isLast();

        return new MatesPageResponse(matesResponses, isLast);
    }


}
