package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.community.mate.dto.response.MateResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewSummaryResponse;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.store.entity.Store;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class StoreDetailResponse {

    @Schema(description = "사용자 ID (비로그인 시 Null)", example = "3", nullable = true)
    private Long userId;

    @Schema(description = "사용자 UUID (비로그인 시 Null)", example = "1c95f3a7-0c7d-4e2b-95cf-ff123abc4567", nullable = true)
    private UUID userUuid;

    @NotNull
    @Schema(description = "가게 ID", example = "12")
    private Long storeId;

    @NotNull
    @Schema(description = "가게 UUID", example = "4e8e1e28-c94e-40d7-8e93-6789abc45678")
    private UUID storeUuid;

    @NotNull
    @Schema(description = "가게 업주 ID", example = "5")
    private Long ownerId;

    @NotNull
    @Schema(description = "가게 업주 UUID", example = "9e48f1a7-71e1-4a8a-b345-123abcde7890")
    private UUID ownerUuid;

    @NotBlank
    @Schema(description = "가게 이름", example = "디저트비 합정점")
    private String name;

    @NotBlank
    @Schema(description = "가게 주소", example = "서울 마포구 양화로 23길 8")
    private String address;

    @NotNull
    @Schema(description = "위도 (소수점 8자리)", example = "37.55687412")
    private BigDecimal latitude;

    @NotNull
    @Schema(description = "경도 (소수점 8자리)", example = "126.92345678")
    private BigDecimal longitude;

    @NotNull
    @Schema(description = "가게 전화번호", example = "02-123-4567")
    private String phone;

    @Schema(description = "반려동물 동반 가능 여부", example = "true", nullable = true)
    private Boolean animalYn;

    @Schema(description = "텀블러 사용 가능 여부", example = "false", nullable = true)
    private Boolean tumblerYn;

    @Schema(description = "주차 가능 여부", example = "true", nullable = true)
    private Boolean parkingYn;

    @Schema(description = "가게 소개글", example = "편안한 분위기의 감성 디저트 카페입니다.", nullable = true)
    private String description;

    @Schema(description = "평균 별점 (소수점 2자리)", example = "4.58")
    private BigDecimal averageRating;

    @NotNull
    @Schema(description = "메뉴 리스트")
    private List<MenuResponse> menus;

    @Schema(description = "가게 대표 이미지 URL 리스트", nullable = true)
    private List<String> storeImages;

    @Schema(description = "업주가 직접 고른 추가 이미지 URL 리스트", nullable = true)
    private List<String> ownerPickImages;

    @Schema(description = "가게 대표 링크", example = "https://instagram.com/dessertbee", nullable = true)
    private String primaryStoreLink;

    @Schema(description = "가게 관련 링크 리스트", example = "[\"https://link1.com\", \"https://link2.com\"]", nullable = true)
    private List<String> storeLinks;

    @NotNull
    @Schema(description = "총 리뷰 개수", example = "128")
    private Integer totalReviewCount;

    @Schema(description = "한줄 리뷰 리스트", nullable = true)
    private List<StoreReviewResponse> storeReviews;

    @NotNull
    @Schema(description = "태그 리스트", example = "[\"케이크\", \"구움과자\"]")
    private List<String> tags;

    @NotNull
    @Schema(description = "운영 시간 정보")
    private List<OperatingHourResponse> operatingHours;

    @Schema(description = "특정 휴무일 정보", nullable = true)
    private List<HolidayResponse> holidays;

    @Schema(description = "가게를 저장한 사용자들의 취향 태그 Top3", example = "[\"비건\", \"키토제닉\",\"락토프리\"]", nullable = true)
    private List<String> topPreferences;

    @Schema(description = "커뮤니티 리뷰 리스트", nullable = true)
    private List<ReviewSummaryResponse> communityReviews;

    @Schema(description = "디저트 메이트 모집글 리스트", nullable = true)
    private List<MateResponse> mate;

    @Schema(description = "저장 여부", example = "true", nullable = true)
    private Boolean saved;

    @Schema(description = "저장 목록 ID", example = "1234", nullable = true)
    private Long savedListId;

    public static StoreDetailResponse fromEntity(Store store, Long userId, UUID userUuid,
                                                 int totalReviewCount,
                                                 List<OperatingHourResponse> operatingHours,
                                                 List<HolidayResponse> holidays,
                                                 List<MenuResponse> menus,
                                                 List<String> storeImages,
                                                 List<String> ownerPickImages,
                                                 List<String> topPreferences,
                                                 List<StoreReviewResponse> storeReviews,
                                                 List<String> tags,
                                                 List<String> storeLinks,
                                                 String primaryStoreLink,
                                                 List<ReviewSummaryResponse> communityReviews,
                                                 List<MateResponse> mate,
                                                 boolean saved,
                                                 Long savedListId) {
        return StoreDetailResponse.builder()
                .userId(userId)
                .userUuid(userUuid)
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .ownerId(store.getOwnerId())
                .ownerUuid(store.getOwnerUuid())
                .name(store.getName())
                .address(store.getAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .phone(store.getPhone())
                .animalYn(store.getAnimalYn())
                .tumblerYn(store.getTumblerYn())
                .parkingYn(store.getParkingYn())
                .description(store.getDescription())
                .averageRating(store.getAverageRating())
                .operatingHours(operatingHours)
                .holidays(holidays)
                .topPreferences(topPreferences)
                .menus(menus)
                .storeImages(storeImages)
                .ownerPickImages(ownerPickImages)
                .primaryStoreLink(primaryStoreLink)
                .storeLinks(storeLinks)
                .totalReviewCount(totalReviewCount)
                .storeReviews(storeReviews)
                .tags(tags)
                .communityReviews(communityReviews)
                .mate(mate)
                .saved(saved)
                .savedListId(savedListId)
                .build();
    }
}
