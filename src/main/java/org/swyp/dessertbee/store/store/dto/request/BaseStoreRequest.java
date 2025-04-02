package org.swyp.dessertbee.store.store.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.swyp.dessertbee.store.store.entity.RegularClosureType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * 가게 요청 DTO의 공통 부분을 담당하는 기본 클래스
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseStoreRequest {
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
    private List<OperatingHourRequest> operatingHours;
    private List<HolidayRequest> holidays;
    private List<? extends StoreLinkRequest> storeLinks;

    /**
     * 가게 링크 요청 기본 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreLinkRequest {
        private String url;
        private Boolean isPrimary;
    }

    /**
     * 휴게시간 요청 클래스
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BreakTimeRequest {
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
    }

    /**
     * 영업 시간 요청 클래스
     */
    @Data
    @SuperBuilder
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

        @Enumerated(EnumType.STRING)
        @Column(length = 10)
        private RegularClosureType regularClosureType;

        @Column(length = 50)
        private String regularClosureWeeks;

        private List<BreakTimeRequest> breakTimes;
    }

    /**
     * 휴무일 요청 클래스
     */
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HolidayRequest {
        private String date;
        private String reason;
    }
}