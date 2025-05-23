package org.swyp.dessertbee.store.schedule.service;

import org.swyp.dessertbee.store.store.dto.request.BaseStoreRequest;
import org.swyp.dessertbee.store.schedule.dto.HolidayResponse;
import org.swyp.dessertbee.store.schedule.dto.OperatingHourResponse;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.schedule.entity.StoreHoliday;

import java.util.List;

public interface StoreScheduleService {
    /**
     * 운영 시간 조회 및 변환
     */
    List<OperatingHourResponse> getOperatingHoursResponse(Long storeId);
    /**
     * 휴무일 조회 및 변환
     */
    List<HolidayResponse> getHolidaysResponse(Long storeId);
    /**
     * 영업 시간 저장/갱신
     */
    void saveOrUpdateOperatingHours(Store store, List<BaseStoreRequest.OperatingHourRequest> operatingHoursRequest);
    /**
     * 휴무일 저장
     */
    List<StoreHoliday> saveHolidays(List<BaseStoreRequest.HolidayRequest> requests, Long storeId);
}
