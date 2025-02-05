package org.swyp.dessertbee.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.entity.StoreStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
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
    private List<Long> tagIds;

    @Builder.Default
    private StoreStatus status = StoreStatus.APPROVED;

    private List<EventCreateRequest> events;  // 이벤트 리스트
    private List<CouponCreateRequest> coupons;  // 쿠폰 리스트
    private List<MenuCreateRequest> menus;  // 메뉴 리스트

    private List<MultipartFile> storeImageFiles;
    private Map<String, List<MultipartFile>> eventImageFiles;
    private Map<String, MultipartFile> menuImageFiles;
}
