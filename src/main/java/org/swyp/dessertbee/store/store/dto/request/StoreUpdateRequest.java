package org.swyp.dessertbee.store.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest {

    @NotNull
    private UUID userUuid;

    @NotBlank
    private String name;

    private String phone;
    private String address;
    private String storeLink;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Boolean animalYn;
    private Boolean tumblerYn;
    private Boolean parkingYn;
    private List<String> notice;
    private List<Long> tagIds;
    private List<MenuRequest> menus;
    private List<StoreCreateRequest.OperatingHourRequest> operatingHours;
    private List<StoreCreateRequest.HolidayRequest> holidays;

    private List<Long> storeImageDeleteIds;
    private List<Long> ownerPickImageDeleteIds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuRequest {
        private UUID menuUuid;
        private String name;
        private BigDecimal price;
        private Boolean isPopular;
        private String description;
        private String imageFileKey;
    }
}
