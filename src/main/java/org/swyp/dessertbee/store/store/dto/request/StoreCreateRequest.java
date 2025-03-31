package org.swyp.dessertbee.store.store.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.store.entity.StoreStatus;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest {

    @NotNull
    private UUID userUuid;

    @NotBlank
    private String name;

    private String phone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Boolean animalYn;
    private Boolean tumblerYn;
    private Boolean parkingYn;
    private List<Long> tagIds;

    @Builder.Default
    private StoreStatus status = StoreStatus.APPROVED;

    private List<String> notice; // 공지사항 리스트 추가

    private List<MenuCreateRequest> menus; // 메뉴 리스트

    private List<MultipartFile> storeImageFiles;  // 가게 대표 이미지
    private List<MultipartFile> ownerPickImageFiles; // 사장님 픽 이미지

    private Map<String, MultipartFile> menuImageFiles;

    @Data
    public static class StoreLinkRequest {
        private String url;
        private Boolean isPrimary;
    }

    private List<StoreLinkRequest> storeLinks;

    private List<OperatingHourRequest> operatingHours; // 영업 시간
    private List<HolidayRequest> holidays; // 휴무 정보

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OperatingHourRequest {
        private DayOfWeek dayOfWeek;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime openingTime;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime closingTime;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime lastOrderTime;

        private Boolean isClosed;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HolidayRequest {
        private String date;
        private String reason;
    }
}
