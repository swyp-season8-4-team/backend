package org.swyp.dessertbee.community.mate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.entity.ReportCategory;
import org.swyp.dessertbee.common.repository.ReportRepository;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.community.mate.dto.request.MateReportRequest;
import org.swyp.dessertbee.community.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.community.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.community.mate.entity.*;
import org.swyp.dessertbee.community.mate.repository.*;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserServiceImpl;

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
    private final SavedMateRepository savedMateRepository;
    private final MateMemberService mateMemberService;
    private final MateReportRepository mateReportRepository;
    private final ReportRepository reportRepository;
    private final StoreRepository storeRepository;
    private final ImageService imageService;
    private final UserServiceImpl userService;


    /** 메이트 등록 */
    @Transactional
    public MateDetailResponse createMate(MateCreateRequest request, MultipartFile mateImage){
        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();


        //userId 존재 여부 확인
        if (user == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

        //위도,경도로 storeId 조회
        // 위도, 경도로 store 조회
        Store store = storeRepository.findByName(request.getPlace().getPlaceName());


        // store가 null이면 storeId는 null, 아니면 store.getStoreId() 할당
        Long storeId = (store != null) ? store.getStoreId() : null;

        Mate mate = mateRepository.save(
                Mate.builder()
                        .userId(user.getId())
                        .storeId(storeId)
                        .mateCategoryId(request.getMateCategoryId())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .recruitYn(Boolean.TRUE.equals(request.getRecruitYn()))
                        .placeName(request.getPlace().getPlaceName())
                        .latitude(request.getPlace().getLatitude())
                        .longitude(request.getPlace().getLongitude())
                        .updatedAt(null)
                        .build()
        );



        //기존 이미지 삭제 후 새 이미지 업로드
        if (mateImage != null && !mateImage.isEmpty()) {
            String folder = "mate/" + mate.getMateId();
            imageService.uploadAndSaveImage(mateImage, ImageType.MATE, mate.getMateId(), folder);
        }

        //디저트 메이트 mateId를 가진 member 데이터 생성
        mateMemberService.addCreatorAsMember(mate.getMateUuid(), user.getId());

        return getMateDetail(mate.getMateUuid());
    }


    /** 메이트 상세 정보 */
    public MateDetailResponse getMateDetail(UUID mateUuid) {
        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();
        //mateId로 디저트메이트 여부 확인
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        // 현재 접속해 있는 사용자의 user 정보 (user가 null일 수 있으므로 null 체크)
        Long currentUserId = (user != null) ? user.getId() : null;


        return mapToMateDetailResponse(mate, currentUserId);

    }

    /** 메이트 삭제 */
    @Transactional
    public void deleteMate(UUID mateUuid) {

        //mateId로 디저트메이트 여부 확인
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        try {
            mate.softDelete();
            mateRepository.save(mate);

            //디저트메이트 멤버 삭제
            mateMemberService.deleteAllMember(mate.getMateId());

            //저장된 디저트메이트 삭제
            savedMateRepository.deleteByMate_MateId(mate.getMateId());

            imageService.deleteImagesByRefId(ImageType.MATE, mate.getMateId());

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

        //mateId 존재 여부 확인
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        //위도,경도로 storeId 조회
        Store store = storeRepository.findByName(request.getPlace().getPlaceName());

        mate.update(request, store);



        //기존 이미지 삭제 후 새 이미지 업로드
        if (mateImage != null && !mateImage.isEmpty()) {
            imageService.updateImage(ImageType.MATE, mate.getMateId(), mateImage, "mate/" + mate.getMateId());
        }

    }

    /**
     * 디저트메이트 전체 조회
     * */
    @Transactional
    public MatesPageResponse getMates(Pageable pageable, String keyword, Long mateCategoryId) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Long currentUserId = (user != null) ? user.getId() : null;

        // 페이지 단위로 메이트 조회 (한 번의 호출로 처리)
        Page<Mate> matesPage = mateRepository.findByDeletedAtIsNullAndMateCategoryId(mateCategoryId, keyword, pageable);

        // 각 메이트 엔티티를 DTO로 변환
        List<MateDetailResponse> mates = matesPage.stream()
                .map(mate -> mapToMateDetailResponse(mate, currentUserId))
                .collect(Collectors.toList());



        return new MatesPageResponse(mates, matesPage.isLast());

    }

    /**
     * 내가 참여한 디저트메이트 조회
     * */
    @Transactional
    public MatesPageResponse getMyMates(Pageable pageable) {
        // 현재 사용자 정보 및 userId 조회
        UserEntity user = userService.getCurrentUser();
        Long userId = userRepository.findIdByUserUuid(user.getUserUuid());

        // 페이지 단위로 참여한 Mate 조회
        Page<Mate> matesPage = mateRepository.findByDeletedAtIsNullAndUserId(pageable, userId);

        // 각 Mate 엔티티를 DTO로 변환
        List<MateDetailResponse> matesResponses = matesPage.stream()
                .map(mate -> mapToMateDetailResponse(mate, userId))
                .collect(Collectors.toList());

        return new MatesPageResponse(matesResponses, matesPage.isLast());
    }


    /**
     * 디저트메이트 신고
     * */
    public void reportMate(UUID mateUuid, MateReportRequest request) {

        //mateId 존재 여부 확인
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));

        Long userId = userRepository.findIdByUserUuid(request.getUserUuid());

        //신고 유무 확인
        MateReport report = mateReportRepository.findByMateIdAndUserId(mate.getMateId(), userId);

        if(report != null){
            throw new DuplicationReportException("이미 신고된 게시물입니다.");
        }


        // 6L로 타입 일치
        // '기타' 신고인 경우 사용자가 입력한 코멘트를 그대로 저장
        if (request.getReportCategoryId().equals(6L)){
            mateReportRepository.save(
                    MateReport.builder()
                            .reportCategoryId(request.getReportCategoryId())
                            .mateId(mate.getMateId())
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
                        .mateId(mate.getMateId())
                        .userId(userId)
                        .comment(reportCategory.getReportComment())
                        .build()
        );


    }

    /**
     * 디저트메이트 정보 조회 중복 코드
     * */
    private MateDetailResponse mapToMateDetailResponse(Mate mate, Long currentUserId) {

        //디저트메이트 사진 조회
        String mateImage = imageService.getImageByTypeAndId(ImageType.MATE, mate.getMateId());

        //mateCategoryId로 name 조회
        String mateCategory = String.valueOf(mateCategoryRepository.findCategoryNameById( mate.getMateCategoryId()));
        //작성자 UUID 조회
        UserEntity creator = mateMemberRepository.findByMateId(mate.getMateId());

        //작성자 프로필 조회
        String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, mate.getUserId());

        Store store = storeRepository.findByName(mate.getPlaceName());

        // 저장 여부 체크
        SavedMate savedMate = (currentUserId != null)
                ? savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), currentUserId)
                : null;
        boolean saved = savedMate != null;

        // 신청 상태 체크
        MateMember applyMember = (currentUserId != null)
                ? mateMemberRepository.findByMateIdAndDeletedAtIsNullAndUserId(mate.getMateId(), currentUserId)
                : null;
        MateApplyStatus applyStatus = (applyMember == null) ? MateApplyStatus.NONE : applyMember.getApplyStatus();

        return MateDetailResponse.fromEntity(mate, mateImage, mateCategory, creator, profileImage, saved, applyStatus, store);

    }

}
