package org.swyp.dessertbee.store.store.dto.response;

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
    private Long storeId;
    private UUID storeUuid;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<OperatingHourResponse> operatingHours; // 운영 시간
    private int shortReviewCount; // 한줄 리뷰 개수
    private List<String> tags; // 태그 리스트
    private String storeImage; // 대표 이미지 (첫 번째 이미지)

    public static StoreMapResponse fromEntity(Store store, List<OperatingHourResponse> operatingHours, int shortReviewCount, List<String> tags, List<String> storeImages) {
        return StoreMapResponse.builder()
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .name(store.getName())
                .address(store.getAddress())
                .latitude(store.getLatitude().doubleValue())
                .longitude(store.getLongitude().doubleValue())
                .operatingHours(operatingHours)
                .shortReviewCount(shortReviewCount)
                .tags(tags)
                .storeImage(storeImages != null && !storeImages.isEmpty() ? storeImages.get(0) : null)
                .build();
    }
}
