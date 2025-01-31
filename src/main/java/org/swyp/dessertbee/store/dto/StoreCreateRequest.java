package org.swyp.dessertbee.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.store.entity.StoreStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest {

    @NotNull
    private Long ownerId;

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
    private String operatingHours;
    private String closingDays;
    private StoreStatus status = StoreStatus.APPROVED;
    private List<String> tags;
}
