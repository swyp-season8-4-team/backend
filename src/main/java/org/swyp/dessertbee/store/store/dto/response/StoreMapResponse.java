package org.swyp.dessertbee.store.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.StoreOperatingHour;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class StoreMapResponse {
    @NotNull
    @Schema(description = "가게 식별자 (PK)", example = "1")
    private Long storeId;

    @NotNull
    @Schema(description = "가게 UUID (고유 식별자)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID storeUuid;

    @NotBlank
    @Schema(description = "가게 이름", example = "디저트비 합정점")
    private String name;

    @NotBlank
    @Schema(description = "가게 주소", example = "서울특별시 마포구 양화로 23길 8")
    private String address;

    @NotNull
    @Schema(description = "위도 (소수점 8자리)", example = "37.55687412")
    private Double latitude;

    @NotNull
    @Schema(description = "경도 (소수점 8자리)", example = "126.92345678")
    private Double longitude;

    @NotNull
    @Schema(description = "운영 시간 리스트", example = "[{\"day\": \"월요일\", \"open\": \"10:00\", \"close\": \"22:00\"}]")
    private List<OperatingHourResponse> operatingHours;

    @Schema(description = "특정 휴무일 정보", nullable = true)
    private List<HolidayResponse> holidays;

    @NotNull
    @Schema(description = "한 줄 리뷰 개수", example = "12")
    private Integer shortReviewCount;

    @NotNull
    @Schema(description = "태그 리스트", example = "[\"케이크\", \"구움과자\"]")
    private List<String> tags;

    @Schema(description = "대표 이미지 URL (첫 번째 이미지)", example = "https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/store/9/3c862a8d-06cf-4516-acd1-68f46207a7d4-store.jpeg", nullable = true)
    private String storeImage;

    public static StoreMapResponse fromEntity(Store store, List<OperatingHourResponse> operatingHours, List<HolidayResponse> holidays, int shortReviewCount, List<String> tags, List<String> storeImages) {
        return StoreMapResponse.builder()
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .name(store.getName())
                .address(store.getAddress())
                .latitude(store.getLatitude().doubleValue())
                .longitude(store.getLongitude().doubleValue())
                .operatingHours(operatingHours)
                .holidays(holidays)
                .shortReviewCount(shortReviewCount)
                .tags(tags)
                .storeImage(storeImages != null && !storeImages.isEmpty() ? storeImages.get(0) : null)
                .build();
    }
}
