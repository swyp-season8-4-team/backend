package org.swyp.dessertbee.community.mate.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.community.mate.entity.Mate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MateRepository extends JpaRepository<Mate, Long> {

    @Transactional
    Optional<Mate> findByMateUuidAndDeletedAtIsNull(UUID mateUuid);

    @Query("SELECT m FROM Mate m WHERE m.storeId = :storeId AND m.deletedAt IS NULL")
    List<Mate> findByStoreIdAndDeletedAtIsNull(@Param("storeId") Long storeId);

    /**
     * MateUuid로 MateId 조회
     * */
    @Query("SELECT m.mateId FROM Mate m where m.mateUuid = :mateUuid")
    Optional<Long> findMateIdByMateUuid(UUID mateUuid);


    /**
     * 디저트 메이트 카테고리로 조회(카테고리 아이디 없을 떄는 null)
     **/
    @Query("SELECT m FROM Mate m " +
            "WHERE m.deletedAt IS NULL " +
            "AND (:mateCategoryId IS NULL OR m.mateCategoryId = :mateCategoryId) " +
            "AND (:keyword IS NULL OR (m.title LIKE CONCAT('%', :keyword, '%') " +
            "     OR m.content LIKE CONCAT('%', :keyword, '%') " +
            "     OR m.placeName LIKE CONCAT('%', :keyword, '%'))) " +
            "AND (:recruitYn IS NULL OR m.recruitYn = :recruitYn) " +
            "ORDER BY m.createdAt DESC")
    Page<Mate> findByDeletedAtIsNullAndMateCategoryIdAndRecruitYn(@Param("mateCategoryId") Long mateCategoryId,
                                                      @Param("keyword") String keyword,
                                                      @Param("recruitYn") Boolean recruitYn,
                                                      Pageable pageable);


    @Query("SELECT m FROM Mate m WHERE m.deletedAt IS NULL AND m.mateId IN :mateIds")
    List<Mate> findByMateIdIn(@Param("mateIds") List<Long> mateIds);

    @Query("SELECT m FROM Mate m WHERE m.deletedAt IS NULL " +
            "AND m.userId = :userId")
    Page<Mate> findByDeletedAtIsNullAndUserId(Pageable pageable, Long userId);
}
