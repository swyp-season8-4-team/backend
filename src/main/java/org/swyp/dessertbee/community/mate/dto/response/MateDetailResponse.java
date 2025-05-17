package org.swyp.dessertbee.community.mate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.community.mate.dto.MatePlace;
import org.swyp.dessertbee.community.mate.entity.Mate;
import org.swyp.dessertbee.community.mate.entity.MateApplyStatus;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MateDetailResponse {

    @NotBlank
    @Schema(description = "디저트메이트 uuid", example = "3037ab04-195e-48d1-83e2-e005899fc74d")
    private UUID mateUuid;

    @NotBlank
    @Schema(description = "가게 ID", example = "12")
    private Long storeId;

    @NotBlank
    @Schema(description = "디저트메이트 작성하는 사람 uuid", example = "19a40ec1-ac92-419e-aa2b-0fcfcbd42447")
    private UUID userUuid;

    @NotBlank(message = "디저트메이트 수용 인원을 선택해주세요.")
    @Schema(description = "디저트메이트 수용 인원", example = "2")
    private Long capacity;

    @NotBlank
    @Schema(description = "디저트메이트 현재 참여 인원", example = "1")
    private Long currentMemberCount;

    @NotBlank
    @Schema(description = "디저트메이트 작성하는 사람 닉네임", example = "디저비1")
    private String nickname;

    @NotBlank
    @Schema(description = "디저트메이트 제목 작성", example = "저랑 같이 홍대 빵지순례할 사람 찾습니다.")
    private String title;

    @NotBlank
    @Schema(description = "디저트메이트 내용 작성",  example = "홍대 빵지순례 리스트 200개 있습니다. 같이 맛있는거 먹으러 가요.")
    private String content;

    @NotNull
    @Schema(description = "디저트메이트 모집 여부", example = "true")
    private Boolean recruitYn;

    @NotBlank
    @Schema(description = "디저트메이트 이미지", example = " mateImage=: https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/mate/75/7edd7706-0bfa-46cf-a6c2-ad67f8a9a440-IMG_8828.jpeg")
    private String mateImage;

    @NotBlank
    @Schema(description = "디저트메이트 생성자 프로필 이미지", example = " mateImage=: https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/profile/75/7edd7706-0bfa-46cf-a6c2-ad67f8a9a440-IMG_8828.jpeg")
    private String profileImage;

    @Schema(description = "디저트메이트 지정 장소", example = " placeName : 모코모코, address : 서울 마포구 와우산로29길 47 1층, latitude: 37.55564710, longitude : 126.92734908")
    private MatePlace place;

    @NotNull
    @Schema(description = "디저트메이트 생성 날짜", example = "2025-03-10 02:15")
    private LocalDateTime createdAt;

    @Schema(description = "디저트메이트 수정 날짜", example = "2025-03-10 02:44")
    private LocalDateTime updatedAt;

    @NotNull
    @Schema(description = "현재 로그인한 사용자의 디저트메이트 저장 유무")
    private boolean saved;

    @NotNull
    @Schema(description = "현재 로그인한 사용자의 디저트메이트 신청 상태값", example = "APPROVED")
    private MateApplyStatus applyStatus;

    @NotBlank
    @Schema(description = "디저트메이트 작성자 성별", example = "FEMALE")
    private UserEntity.Gender gender;

    //디저트메이트 카테고리명
    @NotBlank
    @Schema(description = "디저트메이트 카테고리명", example = "빵지순례")
    private String mateCategory;


    @NotNull
    @Schema(description = "디저트메이트 작성하는 사람 닉네임", example = "디저비1")
    private boolean blockedByAuthorYn;

    public static MateDetailResponse fromEntity(Mate mate,
                                                String mateImage,
                                                String category,
                                                UserEntity creator,
                                                String profileImage,
                                                boolean saved,
                                                MateApplyStatus applyStatus,
                                                Store store,
                                                boolean blockedByAuthorYn) {

        return MateDetailResponse.builder()
                .mateUuid(mate.getMateUuid())
                .storeId(mate.getStoreId())
                .userUuid(creator.getUserUuid())
                .nickname(creator.getNickname())
                .gender(creator.getGender())
                .profileImage(profileImage)
                .title(mate.getTitle())
                .content(mate.getContent())
                .capacity(mate.getCapacity())
                .currentMemberCount(mate.getCurrentMemberCount())
                .recruitYn(mate.getRecruitYn())
                .mateImage(mateImage)
                .mateCategory(category)
                .place(MatePlace.builder()
                        .placeName(store != null ? store.getName() : null)
                        .longitude(store != null ? store.getLongitude() : null)
                        .latitude(store != null ? store.getLatitude() : null)
                        .address(store != null ? store.getAddress() : null)
                        .build())
                .createdAt(mate.getCreatedAt())
                .updatedAt(mate.getUpdatedAt())
                .saved(saved)
                .applyStatus(applyStatus)
                .blockedByAuthorYn(blockedByAuthorYn)
                .build();
    }


}
