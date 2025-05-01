package org.swyp.dessertbee.store.preference.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.preference.dto.TopPreferenceTagResponse;
import org.swyp.dessertbee.store.preference.entity.StoreTopTag;

import java.util.List;

public interface StoreTopTagRepository extends JpaRepository<StoreTopTag, Long> {

    @Query(value = """
    SELECT stt.tag_id AS tagId, p.preference_name AS name, stt.tag_rank AS `rank`
    FROM store_top_tag stt
    JOIN preference p ON stt.tag_id = p.id
    WHERE stt.store_id = :storeId
      AND stt.tag_rank <= 3
    ORDER BY stt.tag_rank ASC
    """, nativeQuery = true)
    List<TopPreferenceTagResponse> findTop3TagsByStoreId(@Param("storeId") Long storeId);

    @Query(value = """
    SELECT stt.tag_id AS tagId, p.preference_name AS name, stt.tag_rank AS `rank`
    FROM store_top_tag stt
    JOIN preference p ON stt.tag_id = p.id
    WHERE stt.store_id = :storeId
      AND stt.tag_rank <= 11
    ORDER BY stt.tag_rank ASC
    """, nativeQuery = true)
    List<TopPreferenceTagResponse> findAllTagsByStoreId(@Param("storeId") Long storeId);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE store_top_tag", nativeQuery = true)
    void truncateStoreTopTag();

    @Modifying
    @Transactional
    @Query(value = """
    INSERT INTO store_top_tag (store_id, tag_id, tag_rank, created_at)
    SELECT store_id, tag_id, tag_rank, NOW()
    FROM (
        SELECT
            store_id,
            preference AS tag_id,
            ROW_NUMBER() OVER (PARTITION BY store_id ORDER BY preference_count DESC) AS tag_rank
        FROM (
            SELECT\s
                ss.store_id,
                sp.preference,
                COUNT(*) AS preference_count
            FROM saved_store_preferences sp
            JOIN saved_store ss ON sp.saved_store_id = ss.id
            GROUP BY ss.store_id, sp.preference
        ) counted
    ) ranked
    WHERE tag_rank <= 11
    """, nativeQuery = true)
    void populateStoreTopTag();
}
