package org.swyp.dessertbee.store.store.dto.response;

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
    private Long storeId;
    private UUID storeUuid;
    private String name;
    private BigDecimal averageRating;
    private List<String> storeImages;
    private List<String> ownerPickImages;
    private List<String> tags;
    private List<OperatingHourResponse> operatingHours;
    private List<HolidayResponse> holidays;
    private List<String> topPreferences;

    private String address;
    private String phone;
    private String storeLink;

    private Boolean animalYn;
    private Boolean tumblerYn;
    private Boolean parkingYn;

    public static StoreSummaryResponse fromEntity(Store store, List<String> tags,
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
                .storeImages(storeImages)
                .ownerPickImages(ownerPickImages)
                .operatingHours(operatingHours)
                .holidays(holidays)
                .topPreferences(topPreferences)
                .address(store.getAddress())
                .phone(store.getPhone())
                .storeLink(store.getStoreLink())
                .animalYn(store.getAnimalYn())
                .tumblerYn(store.getTumblerYn())
                .parkingYn(store.getParkingYn())
                .build();
    }
}
