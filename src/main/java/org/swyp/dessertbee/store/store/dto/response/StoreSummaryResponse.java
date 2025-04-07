package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.store.entity.Store;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class StoreSummaryResponse {
    @NotNull
    @Schema(description = "가게 ID", example = "12")
    private Long storeId;

    @NotNull
    @Schema(description = "가게 UUID", example = "4e8e1e28-c94e-40d7-8e93-6789abc45678")
    private UUID storeUuid;

    @NotBlank
    @Schema(description = "가게 이름", example = "디저트비 합정점")
    private String name;

    @Schema(description = "평균 별점 (소수점 2자리)", example = "4.58")
    private BigDecimal averageRating;

    @Schema(description = "매장 대표 이미지 URL 리스트", nullable = true)
    private List<String> storeImages;

    @Schema(description = "업주가 직접 고른 추가 이미지 URL 리스트", nullable = true)
    private List<String> ownerPickImages;

    @NotNull
    @Schema(description = "태그 리스트", example = "[\"케이크\", \"구움과자\"]")
    private List<String> tags;

    @Schema(description = "가게 대표 링크", example = "https://instagram.com/dessertbee", nullable = true)
    private String primaryStoreLink;

    @Schema(description = "가게 관련 링크 리스트", example = "[\"https://link1.com\", \"https://link2.com\"]", nullable = true)
    private List<String> storeLinks;

    @NotNull
    @Schema(description = "운영 시간 정보")
    private List<OperatingHourResponse> operatingHours;

    @Schema(description = "특정 휴무일 정보", nullable = true)
    private List<HolidayResponse> holidays;

    @Schema(description = "가게를 저장한 사용자들의 취향 태그 Top3", example = "[\"비건\", \"키토제닉\",\"락토프리\"]", nullable = true)
    private List<String> topPreferences;

    @NotBlank
    @Schema(description = "가게 주소", example = "서울 마포구 양화로 23길 8")
    private String address;

    @NotBlank
    @Schema(description = "가게 전화번호", example = "02-123-4567")
    private String phone;

    @Schema(description = "가게 소개글", example = "편안한 분위기의 감성 디저트 카페입니다.", nullable = true)
    private String description;

    @Schema(description = "반려동물 동반 가능 여부", example = "true", nullable = true)
    private Boolean animalYn;

    @Schema(description = "텀블러 사용 가능 여부", example = "false", nullable = true)
    private Boolean tumblerYn;

    @Schema(description = "주차 가능 여부", example = "true", nullable = true)
    private Boolean parkingYn;

    public static StoreSummaryResponse fromEntity(Store store, List<String> tags,
                                                  List<String> storeLinks,
                                                  String primaryStoreLink,
                                                  List<OperatingHourResponse> operatingHours,
                                                  List<HolidayResponse> holidays,
                                                  List<String> storeImages,
                                                  List<String> ownerPickImages,
                                                  List<String> topPreferences) {
        return StoreSummaryResponse.builder()
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .name(store.getName())
                .averageRating(store.getAverageRating())
                .tags(tags)
                .storeLinks(storeLinks)
                .primaryStoreLink(primaryStoreLink)
                .storeImages(storeImages)
                .ownerPickImages(ownerPickImages)
                .operatingHours(operatingHours)
                .holidays(holidays)
                .topPreferences(topPreferences)
                .address(store.getAddress())
                .phone(store.getPhone())
                .description(store.getDescription())
                .animalYn(store.getAnimalYn())
                .tumblerYn(store.getTumblerYn())
                .parkingYn(store.getParkingYn())
                .build();
    }
}
