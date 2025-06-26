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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

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

        // 해당 가게의 모든 휴게시간 조회
        List<StoreBreakTime> breakTimes = storeBreakTimeRepository.findAllByStoreId(storeId);
        Map<Long, List<BreakTimeResponse>> breakTimeMap = breakTimes.stream()
                .collect(Collectors.groupingBy(
                        StoreBreakTime::getOperatingHourId,
                        Collectors.mapping(
                                BreakTimeResponse::fromEntity,
                                Collectors.toList()
                        )
                ));

        return operatingHours.stream()
                .map(oh -> convertToOperatingHourResponse(oh, breakTimeMap.getOrDefault(oh.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    /**
     * 휴무일 조회 및 변환 메서드
     */
    @Override
    public List<HolidayResponse> getHolidaysResponse(Long storeId) {
        List<StoreHoliday> holidays = storeHolidayRepository.findByStoreId(storeId);
        return holidays.stream()
                .map(HolidayResponse::fromEntity)
                .collect(Collectors.toList());
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

        for (BaseStoreRequest.HolidayRequest req : requests) {
            LocalDate startDate = req.getStartDate();
            LocalDate endDate = (req.getEndDate() != null) ? req.getEndDate() : startDate;

            if (startDate == null) {
                throw new StoreExceptions.StoreHolidayTypeException(); // 시작일 누락
            }

            if (endDate.isBefore(startDate)) {
                throw new StoreExceptions.StoreHolidayTermException(); // 종료일이 더 이전인 경우
            }

            holidays.add(StoreHoliday.builder()
                    .storeId(storeId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason(req.getReason())
                    .build());
        }

        return storeHolidayRepository.saveAll(holidays);
    }

    /**
     * 여러 가게의 운영시간 배치 조회
     */
    public Map<Long, List<OperatingHourResponse>> getOperatingHoursBatch(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 모든 가게의 운영시간 조회
        List<StoreOperatingHour> allOperatingHours = storeOperatingHourRepository.findByStoreIdIn(storeIds);

        // 2. 모든 가게의 휴게시간 조회
        List<Object[]> breakTimeResults = storeBreakTimeRepository.findBreakTimesByStoreIds(storeIds);

        // 3. 휴게시간을 operatingHourId별로 그룹핑
        Map<Long, List<BreakTimeResponse>> breakTimeMap = new HashMap<>();
        for (Object[] result : breakTimeResults) {
            Long operatingHourId = (Long) result[1];
            LocalTime startTime = (LocalTime) result[2];
            LocalTime endTime = (LocalTime) result[3];

            BreakTimeResponse breakTime = BreakTimeResponse.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            breakTimeMap.computeIfAbsent(operatingHourId, k -> new ArrayList<>()).add(breakTime);
        }

        // 4. 가게별로 운영시간 응답 생성
        return allOperatingHours.stream()
                .collect(Collectors.groupingBy(
                        StoreOperatingHour::getStoreId,
                        Collectors.mapping(
                                oh -> convertToOperatingHourResponse(oh, breakTimeMap.getOrDefault(oh.getId(), Collections.emptyList())),
                                Collectors.toList()
                        )
                ));
    }

    /**
     * 여러 가게의 휴무일 배치 조회
     */
    public Map<Long, List<HolidayResponse>> getHolidaysBatch(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<StoreHoliday> allHolidays = storeHolidayRepository.findByStoreIdIn(storeIds);

        return allHolidays.stream()
                .collect(Collectors.groupingBy(
                        StoreHoliday::getStoreId,
                        Collectors.mapping(
                                HolidayResponse::fromEntity,
                                Collectors.toList()
                        )
                ));
    }

    /**
     * StoreOperatingHour → OperatingHourResponse 변환 메서드
     */
    private OperatingHourResponse convertToOperatingHourResponse(StoreOperatingHour operatingHour, List<BreakTimeResponse> breakTimes) {
        return OperatingHourResponse.builder()
                .dayOfWeek(operatingHour.getDayOfWeek())
                .openingTime(operatingHour.getOpeningTime())
                .closingTime(operatingHour.getClosingTime())
                .lastOrderTime(operatingHour.getLastOrderTime())
                .isClosed(operatingHour.getIsClosed())
                .regularClosureType(operatingHour.getRegularClosureType() != null ?
                        operatingHour.getRegularClosureType().toString() : null)
                .regularClosureWeeks(operatingHour.getRegularClosureWeeks())
                .breakTimes(breakTimes)
                .build();
    }
}
