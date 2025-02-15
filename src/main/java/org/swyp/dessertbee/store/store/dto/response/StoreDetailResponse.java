package org.swyp.dessertbee.store.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
    private Long storeId;
    private UUID storeUuid;
    private String name;
    private String address;
    private String operatingHours;
    private String closingDays;
    private String phone;
    private String storeLink;
    private Boolean animalYn;
    private Boolean tumblerYn;
    private Boolean parkingYn;
    private BigDecimal averageRating;
    private List<MenuResponse> menus;
    private List<String> storeImages;
    private List<StoreReviewResponse> storeReviews;
    private List<String> tags;

    public static StoreDetailResponse fromEntity(Store store,
                                                 List<MenuResponse> menus,
                                                 List<String> storeImages,
                                                 List<StoreReviewResponse> storeReviews,
                                                 List<String> tags) {
        return StoreDetailResponse.builder()
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .name(store.getName())
                .address(store.getAddress())
                .operatingHours(store.getOperatingHours())
                .closingDays(store.getClosingDays())
                .phone(store.getPhone())
                .storeLink(store.getStoreLink())
                .animalYn(store.getAnimalYn())
                .tumblerYn(store.getTumblerYn())
                .parkingYn(store.getParkingYn())
                .averageRating(store.getAverageRating())
                .menus(menus)  // 메뉴 리스트 추가
                .storeImages(storeImages)
                .storeReviews(storeReviews)
                .tags(tags)
                .build();
    }
}
