package org.swyp.dessertbee.store.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.mate.dto.response.MateResponse;
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
    private Long userId;
    private UUID userUuid;
    private Long storeId;
    private UUID storeUuid;
    private Long ownerId;
    private UUID ownerUuid;
    private String name;
    private String address;
    private String phone;
    private String storeLink;
    private Boolean animalYn;
    private Boolean tumblerYn;
    private Boolean parkingYn;
    private String description;
    private BigDecimal averageRating;
    private List<MenuResponse> menus;
    private List<String> storeImages;
    private List<String> ownerPickImages;
    private int totalReviewCount;
    private List<StoreReviewResponse> storeReviews;
    private List<String> tags;
    private List<String> notice;
    private List<OperatingHourResponse> operatingHours;
    private List<HolidayResponse> holidays;
    private List<String> topPreferences;
    private List<MateResponse> mate;
    private boolean saved;
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
                .phone(store.getPhone())
                .storeLink(store.getStoreLink())
                .animalYn(store.getAnimalYn())
                .tumblerYn(store.getTumblerYn())
                .parkingYn(store.getParkingYn())
                .description(store.getDescription())
                .averageRating(store.getAverageRating())
                .notice(store.getNotice())
                .operatingHours(operatingHours)
                .holidays(holidays)
                .topPreferences(topPreferences)
                .menus(menus)
                .storeImages(storeImages)
                .ownerPickImages(ownerPickImages)
                .totalReviewCount(totalReviewCount)
                .storeReviews(storeReviews)
                .tags(tags)
                .mate(mate)
                .saved(saved)
                .savedListId(savedListId)
                .build();
    }
}
