package org.swyp.dessertbee.mate.service;

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
import org.swyp.dessertbee.mate.dto.request.MateCreateRequest;
import org.swyp.dessertbee.mate.dto.request.MateReportRequest;
import org.swyp.dessertbee.mate.dto.request.MateRequest;
import org.swyp.dessertbee.mate.dto.response.MateDetailResponse;
import org.swyp.dessertbee.mate.dto.response.MatesPageResponse;
import org.swyp.dessertbee.mate.entity.*;
import org.swyp.dessertbee.mate.exception.MateExceptions;
import org.swyp.dessertbee.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.mate.repository.*;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swyp.dessertbee.user.service.UserService;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private final UserService userService;

    /** 메이트 등록 */
    @Transactional
    public MateDetailResponse createMate(MateCreateRequest request, MultipartFile mateImage){

        UserEntity user = userRepository.findByUserUuid(request.getUserUuid());

        //userId 존재 여부 확인
        if (user == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

        //장소명으로 storeId 조회
        Long storeId = storeRepository.findStoreIdByName(request.getPlace().getPlaceName());

        Mate mate = mateRepository.save(
                Mate.builder()
                        .userId(user.getId())
                        .storeId(storeId)
                        .mateCategoryId(request.getMateCategoryId())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .recruitYn(Boolean.TRUE.equals(request.getRecruitYn()))
                        .placeName(request.getPlace().getPlaceName())
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

        return getMateDetail(mate.getMateUuid(), user.getEmail());
    }


    /** 메이트 상세 정보 */
    public MateDetailResponse getMateDetail(UUID mateUuid, String email) {

        UserEntity user = userService.validateUser(email);

        //mateId로 디저트메이트 여부 확인
        Mate mate = mateRepository.findByMateUuidAndDeletedAtIsNull(mateUuid)
                .orElseThrow(() -> new MateNotFoundException("존재하지 않는 디저트메이트입니다."));


        //디저트메이트 사진 조회
        List<String> mateImage = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());

        //mateCategoryId로 name 조회
        String mateCategory = String.valueOf(mateCategoryRepository.findCategoryNameById( mate.getMateCategoryId()));
        //작성자 UUID 조회
        UserEntity creator = mateMemberRepository.findByMateId(mate.getMateId());

        //작성자 프로필 조회
        List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, mate.getUserId());

        //현재 접속해 있는 사용자의 user 정보
        Long userId = userRepository.findIdByUserUuid(user.getUserUuid());


        //저장했는지 유무 확인
        SavedMate savedMate = null;
        if (userId != null) {
            savedMate = savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), userId);
        }
        boolean saved = (savedMate != null);


        System.out.println(userId);
        //신청했는지 유무 확인
        MateMember  member = mateMemberRepository.findByMateIdAndUserId(mate.getMateId(), userId)
                .orElse(null);

        MateApplyStatus applyStatus;

        if(member == null) {
            applyStatus = MateApplyStatus.NONE;
        }else{
            applyStatus = member.getApplyStatus();
        }

        return MateDetailResponse.fromEntity(mate, mateImage, mateCategory, creator, profileImage, saved, applyStatus);

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

        //장소명으로 storeId 조회
        Long storeId = storeRepository.findStoreIdByName(request.getPlace().getPlaceName());

        mate.update(request, storeId);



        //기존 이미지 삭제 후 새 이미지 업로드
        if (mateImage != null && !mateImage.isEmpty()) {
            imageService.updateImage(ImageType.MATE, mate.getMateId(), mateImage, "mate/" + mate.getMateId());
        }

    }

    /**
     * 디저트메이트 전체 조회
     * */
    @Transactional
    public MatesPageResponse getMates(Pageable pageable, String email, String keyword, Long mateCategoryId) {

        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 유저입니다."));



        Page<Mate> mates = mateRepository.findByDeletedAtIsNullAndMateCategoryId( mateCategoryId, keyword,pageable);


        List<MateDetailResponse> matesResponses = mateRepository.findByDeletedAtIsNullAndMateCategoryId( mateCategoryId, keyword, pageable)
                    .stream()
                    .map(mate -> {
                        List<String> mateImages = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());
                        String mateCategory = mateCategoryRepository.findCategoryNameById(mate.getMateCategoryId());
                        UserEntity creator = mateMemberRepository.findByMateId(mate.getMateId());
                        //사용자 프로필 조회
                        List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, mate.getUserId());


                        //현재 접속해 있는 사용자의 user 정보
                        Long userId = userRepository.findIdByUserUuid(user.getUserUuid());

                        SavedMate savedMate = null;
                        if (userId != null) {
                            savedMate = savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), userId);
                        }
                        boolean saved = (savedMate != null);

                        //신청했는지 유무 확인
                        MateMember applyMember = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndUserId(mate.getMateId(), userId);

                        MateApplyStatus applyStatus;
                        if (applyMember == null) {
                            // 가입(신청) 안 했으므로, NONE 상태로 처리
                            applyStatus = MateApplyStatus.NONE;
                        } else {
                            // 가입(신청)한 경우, 실제 상태를 가져옴
                            applyStatus = applyMember.getApplyStatus();
                        }

                        return MateDetailResponse.fromEntity(mate, mateImages, mateCategory, creator, profileImage, saved, applyStatus);

                    })
                    .collect(Collectors.toList());

        // 다음 페이지 존재 여부 확인
        boolean isLast = mates.isLast();

        return new MatesPageResponse(matesResponses, isLast);

    }

    /**
     * 내가 참여한 디저트메이트 조회
     * */
    public MatesPageResponse getMyMates(Pageable pageable,String email) {

        UserEntity user = userService.validateUser(email);
        Long userId = userRepository.findIdByUserUuid(user.getUserUuid());


        Page<Mate> mates = mateRepository.findByDeletedAtIsNullAndUserId(pageable, userId);


        List<MateDetailResponse> matesResponses = mateRepository.findByDeletedAtIsNullAndUserId(pageable, userId)
                .stream()
                .map(mate -> {
                    List<String> mateImages = imageService.getImagesByTypeAndId(ImageType.MATE, mate.getMateId());
                    String mateCategory = mateCategoryRepository.findCategoryNameById(mate.getMateCategoryId());
                    UserEntity creator = mateMemberRepository.findByMateId(mate.getMateId());
                    //사용자 프로필 조회
                    List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, mate.getUserId());


                    SavedMate savedMate = null;
                    if (userId != null) {
                        savedMate = savedMateRepository.findByMate_MateIdAndUserId(mate.getMateId(), userId);
                    }
                    boolean saved = (savedMate != null);

                    //신청했는지 유무 확인
                    MateMember applyMember = mateMemberRepository.findByMateIdAndDeletedAtIsNullAndUserId(mate.getMateId(), userId);

                    return MateDetailResponse.fromEntity(mate, mateImages, mateCategory, creator, profileImage, saved, applyMember.getApplyStatus());

                })
                .collect(Collectors.toList());

        // 다음 페이지 존재 여부 확인
        boolean isLast = mates.isLast();

        return new MatesPageResponse(matesResponses, isLast);
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

}
