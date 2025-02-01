package org.swyp.dessertbee.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.entity.Store;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class StoreDetailResponse {
    private Long id;
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
    private List<EventResponse> events;
    private List<MenuResponse> menus;
    private List<CouponResponse> coupons;
    private List<String> storeImages;
    private List<String> eventImages;
    private List<String> menuImages;
    private List<StoreReviewResponse> storeReviews;
    private List<String> tags;

    public static StoreDetailResponse fromEntity(Store store,
                                                 List<EventResponse> events,
                                                 List<CouponResponse> coupons,
                                                 List<String> storeImages,
                                                 List<String> eventImages,
                                                 List<String> menuImages,
                                                 List<StoreReviewResponse> storeReviews,
                                                 List<String> tags) {
        return StoreDetailResponse.builder()
                .id(store.getId())
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
                .events(events)
                .coupons(coupons)
                .storeImages(storeImages)
                .eventImages(eventImages)
                .menuImages(menuImages)
                .storeReviews(storeReviews)
                .tags(tags)
                .build();
    }
}
