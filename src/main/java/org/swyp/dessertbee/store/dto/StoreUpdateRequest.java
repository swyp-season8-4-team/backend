package org.swyp.dessertbee.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest {

    private String phone; // 전화번호
    private String address; // 주소
    private String storeLink; // 인스타그램 링크
    private BigDecimal latitude; // 위도
    private BigDecimal longitude; // 경도
    private String description; // 소개글
    private Boolean animalYn; // 애견 동반 여부
    private Boolean tumblerYn; // 텀블러 할인 여부
    private Boolean parkingYn; // 주차공간 여부
    private String operatingHours; // 영업시간
    private String closingDays; // 휴무일
    private List<String> tags; // 태그 리스트
}
