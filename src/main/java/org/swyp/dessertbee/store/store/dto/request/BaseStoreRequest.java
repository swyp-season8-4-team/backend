package org.swyp.dessertbee.store.store.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "사용자 UUID (비로그인 시 Null)", example = "1c95f3a7-0c7d-4e2b-95cf-ff123abc4567")
    private UUID userUuid;

    @NotBlank
    @Schema(description = "가게 이름", example = "디저트비 합정점")
    private String name;

    @Schema(description = "가게 전화번호", example = "02-123-4567")
    private String phone;

    @Schema(description = "가게 주소", example = "서울 마포구 양화로 23길 8")
    private String address;

    @Schema(description = "위도 (소수점 8자리)", example = "37.55687412")
    private BigDecimal latitude;

    @Schema(description = "경도 (소수점 8자리)", example = "126.92345678")
    private BigDecimal longitude;

    @Schema(description = "가게 소개글", example = "편안한 분위기의 감성 디저트 카페입니다.")
    private String description;

    @Schema(description = "반려동물 동반 가능 여부", example = "true")
    private Boolean animalYn;

    @Schema(description = "텀블러 사용 가능 여부", example = "false")
    private Boolean tumblerYn;

    @Schema(description = "주차 가능 여부", example = "true")
    private Boolean parkingYn;

    @Schema(description = "태그 ID 리스트", example = "[1, 3, 5]")
    private List<Long> tagIds;

    @Schema(description = "운영 시간 정보")
    private List<OperatingHourRequest> operatingHours;

    @Schema(description = "특정 휴무일 정보")
    private List<HolidayRequest> holidays;

    @Schema(description = "가게 관련 링크 리스트", example = "[\"https://link1.com\", \"https://link2.com\"]")
    private List<? extends StoreLinkRequest> storeLinks;

    /**
     * 가게 링크 요청 기본 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreLinkRequest {
        @Schema(description = "가게 링크")
        private String url;

        @Schema(description = "대표 링크 여부 (대표 링크면 true)")
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
        @Schema(description = "휴게 시작 시간", example = "14:00")
        private LocalTime startTime;

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "휴게 종료 시간", example = "15:00")
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
        @Schema(description = "요일", example = "MONDAY")
        private DayOfWeek dayOfWeek;

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "오픈 시간", example = "10:00")
        private LocalTime openingTime;

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "마감 시간", example = "20:00")
        private LocalTime closingTime;

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "라스트 오더 시간", example = "19:30")
        private LocalTime lastOrderTime;

        @Schema(description = "해당 요일 휴무 여부", example = "false")
        private Boolean isClosed;

        @Enumerated(EnumType.STRING)
        @Column(length = 10)
        @Schema(description = "정기 휴무 유형", example = "MONTHLY")
        private RegularClosureType regularClosureType;

        @Column(length = 50)
        @Schema(description = "정기 휴무 주차", example = "1,3")
        private String regularClosureWeeks;

        @Schema(description = "휴게 시간 목록")
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
        @Schema(description = "휴무 일자", example = "2025-01-01")
        private String date;

        @Schema(description = "휴무 사유", example = "신정")
        private String reason;
    }
}