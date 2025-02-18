package org.swyp.dessertbee.store.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.StoreHoliday;
import org.swyp.dessertbee.store.store.entity.StoreOperatingHour;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
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
    private String phone;
    private String storeLink;
    private Boolean animalYn;
    private Boolean tumblerYn;
    private Boolean parkingYn;
    private BigDecimal averageRating;
    private List<MenuResponse> menus;
    private List<String> storeImages;
    private List<String> ownerPickImages; // 사장님 픽 이미지 추가
    private List<StoreReviewResponse> storeReviews;
    private List<String> tags;
    private List<String> notice;
    private List<OperatingHourResponse> operatingHours;
    private List<HolidayResponse> holidays;

    @Data
    @Builder
    @AllArgsConstructor
    public static class OperatingHourResponse {
        private DayOfWeek dayOfWeek;
        private LocalTime openingTime;
        private LocalTime closingTime;
        private LocalTime lastOrderTime;
        private Boolean isClosed;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class HolidayResponse {
        private String date;
        private String reason;
    }

    public static StoreDetailResponse fromEntity(Store store,
                                                 List<OperatingHourResponse> operatingHours,
                                                 List<HolidayResponse> holidays,
                                                 List<MenuResponse> menus,
                                                 List<String> storeImages,
                                                 List<String> ownerPickImages,
                                                 List<StoreReviewResponse> storeReviews,
                                                 List<String> tags) {
        return StoreDetailResponse.builder()
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .storeLink(store.getStoreLink())
                .animalYn(store.getAnimalYn())
                .tumblerYn(store.getTumblerYn())
                .parkingYn(store.getParkingYn())
                .averageRating(store.getAverageRating())
                .notice(store.getNotice())
                .operatingHours(operatingHours)
                .holidays(holidays)
                .menus(menus)
                .storeImages(storeImages)
                .ownerPickImages(ownerPickImages)
                .storeReviews(storeReviews)
                .tags(tags)
                .build();
    }
}
