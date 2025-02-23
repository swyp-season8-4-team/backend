package org.swyp.dessertbee.mate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.mate.entity.Mate;
import org.swyp.dessertbee.mate.exception.MateExceptions;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.mate.repository.MateCategoryRepository;
import org.swyp.dessertbee.mate.repository.MateMemberRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MateService {

    private final UserRepository userRepository;
    private final MateRepository mateRepository;
    private final MateMemberRepository mateMemberRepository;
    private final MateCategoryRepository mateCategoryRepository;
    private final MateMemberService mateMemberService;
    private final StoreRepository storeRepository;
    private final ImageService imageService;

    /** 메이트 등록 */
    @Transactional
    public MateDetailResponse createMate(MateCreateRequest request, MultipartFile mateImage){

        Long userId = userRepository.findIdByUserUuid(request.getUserUuid());

        //userId 존재 여부 확인
        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

        //장소명으로 storeId 조회
        Long storeId = storeRepository.findStoreIdByName(request.getPlace().getPlaceName());

        Mate mate = mateRepository.save(
                Mate.builder()
                        .userId(userId)
                        .storeId(storeId)
                        .mateCategoryId(request.getMateCategoryId())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .recruitYn(Boolean.TRUE.equals(request.getRecruitYn()))
                        .placeName(request.getPlace().getPlaceName())
                        .build()
        );



        //기존 이미지 삭제 후 새 이미지 업로드
        if (mateImage != null && !mateImage.isEmpty()) {
            String folder = "mate/" + mate.getMateId();
            imageService.uploadAndSaveImage(mateImage, ImageType.MATE, mate.getMateId(), folder);
        }

        //디저트 메이트 mateId를 가진 member 데이터 생성
        mateMemberService.addCreatorAsMember(mate.getMateUuid(), userId);

        return getMateDetail(mate.getMateUuid());
    }


    /** 메이트 상세 정보 */
    public MateDetailResponse getMateDetail(UUID mateUuid) {

        //mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);

        //mateId로 디저트메이트 여부 확인
        Mate mate = mateRepository.findByMateIdAndDeletedAtIsNull(mateId)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        //디저트메이트 사진 조회
        List<String> mateImage = imageService.getImagesByTypeAndId(ImageType.MATE, mateId);

        //mateCategoryId로 name 조회
        String mateCategory = String.valueOf(mateCategoryRepository.findCategoryNameById( mate.getMateCategoryId()));


        // 사용자 UUID 조회
        UserEntity creator = mateMemberRepository.findByMateId(mate.getMateId());

        //사용자 프로필 조회
        List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, mate.getUserId());

        return MateDetailResponse.fromEntity(mate, mateImage, mateCategory, creator, profileImage);

    }

    /** 메이트 삭제 */
    @Transactional
    public void deleteMate(UUID mateUuid) {

        //mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);

        //mateId 존재 여부 확인
        Mate mate = mateRepository.findByMateIdAndDeletedAtIsNull(mateId)
                .orElseThrow(() -> new MateNotFoundException("존재하지 !않는 디저트메이트입니다."));

        try {
                mate.softDelete();
                mateRepository.save(mate);

                //디저트메이트 멤버 삭제
                mateMemberService.deleteAllMember(mateId);

                imageService.deleteImagesByRefId(ImageType.MATE, mateId);

        } catch (Exception e) {
                System.out.println("❌ S3 이미지 삭제 중 오류 발생: " + e.getMessage());
                throw new RuntimeException("S3 이미지 삭제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 메이트 수정
     * */
    @Transactional
    public void updateMate(UUID mateUuid, MateCreateRequest request, MultipartFile mateImage) {
        //mateUuid로 mateId 조회
        Long mateId = mateRepository.findMateIdByMateUuid(mateUuid);

        //mateId 존재 여부 확인
        Mate mate = mateRepository.findByMateIdAndDeletedAtIsNull(mateId)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        mate.update(request);



        //기존 이미지 삭제 후 새 이미지 업로드
        if (mateImage != null && !mateImage.isEmpty()) {
            imageService.updateImage(ImageType.MATE,mateId, mateImage, "mate/" + mateId);
        }

    }

    /**
     * 디저트메이트 전체 조회
     * */
    @Transactional
    public MatesPageResponse getMates(int from, int to) {


        if (from >= to) {
            throw new FromToMateException("잘못된 범위 요청입니다.");
        }

        int limit = to - from;


        // limit + 1 만큼 데이터를 가져와서 다음 데이터가 있는지 확인
        List<Mate> mates = mateRepository.findAllByDeletedAtIsNull(from, limit + 1);


        List<MateDetailResponse> matesResponses =mateRepository.findAllByDeletedAtIsNull(from, limit)
                    .stream()
                    .map(mate -> {
                        List<String> mateImages = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());
                        String mateCategory = mateCategoryRepository.findCategoryNameById(mate.getMateCategoryId());
                        UserEntity creator = mateMemberRepository.findByMateId(mate.getMateId());
                        //사용자 프로필 조회
                        List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, mate.getUserId());

                        return MateDetailResponse.fromEntity(mate, mateImages, mateCategory, creator, profileImage);
                    })
                    .collect(Collectors.toList());

        // limit보다 적은 개수가 조회되면 마지막 데이터임
        boolean isLast = mates.size() <= limit;

        return new MatesPageResponse(matesResponses, isLast);

    }



}
