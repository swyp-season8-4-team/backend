package org.swyp.dessertbee.store.schedule.service;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.store.dto.request.BaseStoreRequest;
import org.swyp.dessertbee.store.schedule.dto.BreakTimeResponse;
import org.swyp.dessertbee.store.schedule.dto.HolidayResponse;
import org.swyp.dessertbee.store.schedule.dto.OperatingHourResponse;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.schedule.entity.StoreBreakTime;
import org.swyp.dessertbee.store.schedule.entity.StoreHoliday;
import org.swyp.dessertbee.store.schedule.entity.StoreOperatingHour;
import org.swyp.dessertbee.store.store.exception.StoreExceptions;
import org.swyp.dessertbee.store.schedule.repository.StoreBreakTimeRepository;
import org.swyp.dessertbee.store.schedule.repository.StoreHolidayRepository;
import org.swyp.dessertbee.store.schedule.repository.StoreOperatingHourRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreScheduleServiceImpl implements StoreScheduleService {
    private final StoreOperatingHourRepository storeOperatingHourRepository;
    private final StoreBreakTimeRepository storeBreakTimeRepository;
    private final StoreHolidayRepository storeHolidayRepository;

    /**
     * 운영 시간 조회 및 변환 메서드
     */
    @Operation
    public List<OperatingHourResponse> getOperatingHoursResponse(Long storeId) {
        List<StoreOperatingHour> operatingHours = storeOperatingHourRepository.findByStoreId(storeId);

        return operatingHours.stream()
                .map(o -> {
                    // 휴게시간 조회
                    List<BreakTimeResponse> breakTimes = storeBreakTimeRepository.findByOperatingHourId(o.getId())
                            .stream()
                            .map(b -> BreakTimeResponse.builder()
                                    .startTime(b.getStartTime())
                                    .endTime(b.getEndTime())
                                    .build())
                            .toList();

                    return OperatingHourResponse.builder()
                            .dayOfWeek(o.getDayOfWeek())
                            .openingTime(o.getOpeningTime())
                            .closingTime(o.getClosingTime())
                            .lastOrderTime(o.getLastOrderTime())
                            .isClosed(o.getIsClosed())
                            .regularClosureType(o.getRegularClosureType() != null ? o.getRegularClosureType().name() : null)
                            .regularClosureWeeks(o.getRegularClosureWeeks())
                            .breakTimes(breakTimes)
                            .build();
                })
                .toList();
    }

    /**
     * 휴무일 조회 및 변환 메서드
     */
    @Override
    public List<HolidayResponse> getHolidaysResponse(Long storeId) {
        List<StoreHoliday> holidays = storeHolidayRepository.findByStoreId(storeId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        if (holidays.isEmpty()) return Collections.emptyList();

        return holidays.stream()
                .sorted(Comparator.comparing(StoreHoliday::getHolidayDate)) // 날짜 오름차순 정렬
                .map(h -> HolidayResponse.builder()
                        .date(h.getHolidayDate().format(formatter))
                        .reason(h.getReason())
                        .build())
                .toList();
    }

    /**
     * 영업 시간 저장/갱신 메서드
     */
    @Override
    public void saveOrUpdateOperatingHours(Store store, List<BaseStoreRequest.OperatingHourRequest> operatingHoursRequest) {
        if (operatingHoursRequest != null) {
            // 기존 영업 시간 데이터 삭제
            storeOperatingHourRepository.deleteByStoreId(store.getStoreId());

            // 기존 휴게 시간 데이터 삭제
            storeBreakTimeRepository.deleteByOperatingHourIdIn(
                    storeOperatingHourRepository.findIdsByStoreId(store.getStoreId())
            );

            // 영업 시간 데이터 저장
            List<StoreOperatingHour> operatingHours = operatingHoursRequest.stream()
                    .map(hour -> StoreOperatingHour.builder()
                            .storeId(store.getStoreId())
                            .dayOfWeek(hour.getDayOfWeek())
                            .openingTime(hour.getOpeningTime())
                            .closingTime(hour.getClosingTime())
                            .lastOrderTime(hour.getLastOrderTime())
                            .isClosed(hour.getIsClosed())
                            .regularClosureType(hour.getRegularClosureType())
                            .regularClosureWeeks(hour.getRegularClosureWeeks())
                            .build())
                    .toList();

            List<StoreOperatingHour> savedOperatingHours = storeOperatingHourRepository.saveAll(operatingHours);

            // 휴게 시간 데이터 저장
            List<StoreBreakTime> breakTimes = new ArrayList<>();
            for (int i = 0; i < operatingHoursRequest.size(); i++) {
                BaseStoreRequest.OperatingHourRequest hourRequest = operatingHoursRequest.get(i);
                StoreOperatingHour savedHour = savedOperatingHours.get(i);

                if (hourRequest.getBreakTimes() != null && !hourRequest.getBreakTimes().isEmpty()) {
                    List<StoreBreakTime> dayBreakTimes = hourRequest.getBreakTimes().stream()
                            .map(breakTime -> StoreBreakTime.builder()
                                    .operatingHourId(savedHour.getId())
                                    .startTime(breakTime.getStartTime())
                                    .endTime(breakTime.getEndTime())
                                    .build())
                            .toList();
                    breakTimes.addAll(dayBreakTimes);
                }
            }

            if (!breakTimes.isEmpty()) {
                storeBreakTimeRepository.saveAll(breakTimes);
            }
        }
    }

    /**
     * 휴무일 저장
     * @param requests 휴무일 요청 리스트
     * @param storeId 가게 ID
     * @return 저장할 StoreHoliday 리스트
     */
    @Override
    public List<StoreHoliday> saveHolidays(List<BaseStoreRequest.HolidayRequest> requests, Long storeId) {
        // 기존 휴무일 모두 삭제
        storeHolidayRepository.deleteByStoreId(storeId);

        // 새로 저장할 휴무일이 없으면 빈 리스트 반환
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<StoreHoliday> holidays = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        for (BaseStoreRequest.HolidayRequest req : requests) {
            String dateStr = req.getDate(); // 예: 2025.02.10-2025.02.14 또는 2025.02.14
            String reason = req.getReason();

            LocalDate startDate;
            LocalDate endDate;

            try {
                String[] parts = dateStr.split("-");
                startDate = LocalDate.parse(parts[0], formatter);
                endDate = (parts.length == 2)
                        ? LocalDate.parse(parts[1], formatter)
                        : startDate;
            } catch (DateTimeParseException e) {
                throw new StoreExceptions.StoreHolidayTypeException(); // 잘못된 날짜 형식
            }

            if (endDate.isBefore(startDate)) {
                throw new StoreExceptions.StoreHolidayTermException(); // 종료일이 시작일보다 빠름
            }

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                holidays.add(StoreHoliday.builder()
                        .storeId(storeId)
                        .holidayDate(date)
                        .reason(reason)
                        .build());
            }
        }

        return storeHolidayRepository.saveAll(holidays);
    }
}
