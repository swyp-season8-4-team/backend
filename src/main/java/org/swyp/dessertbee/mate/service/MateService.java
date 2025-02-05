package org.swyp.dessertbee.mate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.mate.entity.Mate;
import org.swyp.dessertbee.mate.repository.MateCategoryRepository;
import org.swyp.dessertbee.mate.repository.MateRepository;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class MateService {

    private final MateRepository mateRepository;
    private final MateCategoryRepository mateCategoryRepository;
    private final ImageService imageService;


    /** 가게 등록 */
    public MateDetailResponse createMate(MateCreateRequest request, List<MultipartFile> mateImageFiles) {

        Mate mate = mateRepository.save(
                Mate.builder()
                        .userId(request.getUserId())
                        .mateCategoryId(request.getMateCategoryId())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .recruitYn(Boolean.TRUE.equals(request.getRecruitYn()))
                        .build()
        );

        // 가게 대표 사진 S3 업로드 및 저장
        if (mateImageFiles != null && !mateImageFiles.isEmpty()) {
            String folder = "mate/" + mate.getMateId();
            imageService.uploadAndSaveImages(mateImageFiles, ImageType.MATE, mate.getMateId(), folder);
        }
        return getMateDetails(mate.getMateId());
    }


    /** 메이트 상세 정보 */
    public MateDetailResponse getMateDetails(Long mateId) {
        Mate mate;

        Optional<Mate> optionalMate = mateRepository.findById(mateId);


        if(optionalMate.isPresent()) {
            mate =  optionalMate.get();
        }else {
            throw new IllegalArgumentException("존재하지 않는 디저트메이트입니다.");
        }

        //디저트메이트 사진 조회
        List<String> mateImage = imageService.getImagesByTypeAndId(ImageType.MATE, mateId);

        //mateCategoryId로 name 조회
        String mateCategory = String.valueOf(mateCategoryRepository.findCategoryNameById( mate.getMateCategoryId()));

        return MateDetailResponse.fromEntity(mate, mateImage, mateCategory);

    }

    /** 메이트 삭제 */

        @Transactional
        public void deleteMate(Long mateId) {
            Mate mate = mateRepository.findByMateIdAndDeletedAtIsNull(mateId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 디저트메이트입니다."));

            try {
                mate.softDelete();
                mateRepository.save(mate);

                imageService.deleteImagesByRefId(ImageType.MATE, mateId);

            } catch (Exception e) {
                System.out.println("❌ S3 이미지 삭제 중 오류 발생: " + e.getMessage());
                throw new RuntimeException("S3 이미지 삭제 실패: " + e.getMessage(), e);
            }
        }


}
