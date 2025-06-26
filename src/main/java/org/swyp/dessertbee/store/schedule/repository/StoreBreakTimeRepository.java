package org.swyp.dessertbee.store.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.dessertbee.store.schedule.entity.StoreBreakTime;

import java.util.List;

/**
 * 매장 휴게시간 정보 Repository
 */
public interface StoreBreakTimeRepository extends JpaRepository<StoreBreakTime, Long> {

    /**
     * 영업시간 ID로 휴게시간 목록 조회
     */
    List<StoreBreakTime> findByOperatingHourId(Long operatingHourId);

    /**
     * 영업시간 ID 목록으로 휴게시간 목록 조회
     */
    List<StoreBreakTime> findByOperatingHourIdIn(List<Long> operatingHourIds);

    /**
     * 여러 가게의 휴게시간을 한 번에 조회
     */
    @Query("SELECT oh.storeId, bt.operatingHourId, bt.startTime, bt.endTime " +
            "FROM StoreBreakTime bt " +
            "JOIN StoreOperatingHour oh ON bt.operatingHourId = oh.id " +
            "WHERE oh.storeId IN :storeIds")
    List<Object[]> findBreakTimesByStoreIds(@Param("storeIds") List<Long> storeIds);

    /**
     * 영업시간 ID로 휴게시간 데이터 삭제
     */
    @Modifying
    void deleteByOperatingHourId(Long operatingHourId);

    /**
     * 영업시간 ID 목록으로 휴게시간 데이터 일괄 삭제
     */
    @Modifying
    void deleteByOperatingHourIdIn(List<Long> operatingHourIds);

    /**
     * 매장 ID와 연결된 모든 휴게시간 조회 (StoreOperatingHour를 통한 조인)
     */
    @Query("SELECT bt FROM StoreBreakTime bt " +
            "JOIN StoreOperatingHour oh ON bt.operatingHourId = oh.id " +
            "WHERE oh.storeId = :storeId")
    List<StoreBreakTime> findAllByStoreId(@Param("storeId") Long storeId);

    /**
     * 매장 ID와 연결된 모든 휴게시간 삭제 (StoreOperatingHour를 통한 조인)
     */
    @Modifying
    @Query("DELETE FROM StoreBreakTime bt " +
            "WHERE bt.operatingHourId IN " +
            "(SELECT oh.id FROM StoreOperatingHour oh WHERE oh.storeId = :storeId)")
    void deleteAllByStoreId(@Param("storeId") Long storeId);
}