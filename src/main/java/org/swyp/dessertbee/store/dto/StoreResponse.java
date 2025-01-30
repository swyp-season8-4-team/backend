package org.swyp.dessertbee.store.dto;

import org.swyp.dessertbee.store.entity.Store;
import org.swyp.dessertbee.store.entity.StoreStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StoreResponse(
        Long id,
        Long ownerId,
        String name,
        String phone,
        String address,
        String storeLink,
        BigDecimal latitude,
        BigDecimal longitude,
        String description,
        Boolean animalYn,
        Boolean tumblerYn,
        Boolean parkingYn,
        String operatingHours,
        String closingDays,
        BigDecimal averageRating,
        StoreStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> tags
) {
    public StoreResponse(Store store, List<String> tags) {
        this(
                store.getId(),
                store.getOwnerId(),
                store.getName(),
                store.getPhone(),
                store.getAddress(),
                store.getStoreLink(),
                store.getLatitude(),
                store.getLongitude(),
                store.getDescription(),
                store.getAnimalYn(),
                store.getTumblerYn(),
                store.getParkingYn(),
                store.getOperatingHours(),
                store.getClosingDays(),
                store.getAverageRating(),
                store.getStatus(),
                store.getCreatedAt(),
                store.getUpdatedAt(),
                tags
        );
    }
}
