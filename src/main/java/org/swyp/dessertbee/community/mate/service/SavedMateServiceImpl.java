package org.swyp.dessertbee.community.mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.community.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateApplyStatus;
import org.swyp.dessertbee.community.mate.entity.MateMember;
import org.swyp.dessertbee.community.mate.entity.SavedMate;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.community.mate.repository.MateCategoryRepository;
import org.swyp.dessertbee.community.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.community.mate.repository.MateRepository;
import org.swyp.dessertbee.community.mate.repository.SavedMateRepository;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.user.service.UserBlockService;
import org.swyp.dessertbee.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedMateServiceImpl implements SavedMateService {

    private final MateRepository mateRepository;
    private final SavedMateRepository savedMateRepository;
    private final MateMemberRepository mateMemberRepository;
    private final MateCategoryRepository mateCategoryRepository;
    private final ImageService imageService;
    private final UserService userService;
    private final StoreRepository storeRepository;
    private final UserBlockService userBlockService;

    /**
     * 디저트메이트 저장
     * */
    @Override
    @Transactional
    public void saveMate(UUID mateUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        Long userId = user.getId();
        try {
            userService.findById(userId);

            SavedMate savedMate = savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), userId);
            if(savedMate != null) {
                throw new DuplicationSavedMateException("이미 저장된 디저트메이트입니다.");
            }

            savedMateRepository.save(
                    SavedMate.builder()
                            .mate(mate)
                            .userId(user.getId())
                            .build()
            );

        } catch (BusinessException e) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }


    }

    /**
     * 디저트메이트 삭제
     * */
    @Override
    @Transactional
    public void deleteSavedMate(UUID mateUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        Long userId = user.getId();



        try {
            userService.findById(userId);

            SavedMate savedMate = savedMateRepository.findByMate_MateIdAndUserId(mateId, userId);
            if(savedMate == null) {
                throw new SavedMateNotFoundException("이미 저장된 디저트메이트입니다.");
            }

            savedMateRepository.delete(savedMate);
        } catch (BusinessException e) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }

    }

    /**
     * 저장된 디저트메이트 조회 (userId에 해당하는 Mate만 가져오기)
     */
    public MatesPageResponse getSavedMates(Pageable pageable) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Long userId = user.getId();


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
                    String mateImage = imageService.getImageByTypeAndId(ImageType.MATE, mate.getMateId());

                    String mateCategory = mateCategoryRepository.findCategoryNameById(mate.getMateCategoryId());

                    UserEntity creator = userService.findById(mate.getUserId());

                    String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, mate.getUserId());

                    boolean blockedByAuthorYn = userBlockService.isBlocked(user.getUserUuid(), creator.getUserUuid());

                    Store store = storeRepository.findByName(mate.getPlaceName());

                    //저장된 디저트메이트 데이터만 지고 오는거니까 true
                    boolean saved = true;


                    MateMember applyMember = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndUserId(mate.getMateId(), userId);
                    MateApplyStatus applyStatus = (applyMember == null) ? null : applyMember.getApplyStatus();

                    return MateDetailResponse.fromEntity(mate, mateImage, mateCategory, creator, profileImage, saved,applyStatus, store, blockedByAuthorYn);
                })
                .collect(Collectors.toList());

        // 4️⃣ 다음 페이지 존재 여부 확인
        boolean isLast = savedMates.isLast();

        return new MatesPageResponse(matesResponses, isLast);
    }


}
