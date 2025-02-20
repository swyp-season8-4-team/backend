package org.swyp.dessertbee.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.mate.entity.Mate;
import org.swyp.dessertbee.mate.entity.MateReply;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateReplyRepository extends JpaRepository<MateReply, Long> {

    /**
     * 디저트메이트 댓글 전체 조회(삭제된 댓글 제외)
     * */
    @Query("SELECT m FROM MateReply m WHERE m.deletedAt IS NULL AND m.mateId = :mateId ORDER BY m.mateReplyId ASC LIMIT :limit OFFSET :from")
    List<MateReply> findAllByDeletedAtIsNull(Long mateId, int from, int limit);

    Optional<MateReply> findByMateIdAndDeletedAtIsNull(Long replyId);
}
