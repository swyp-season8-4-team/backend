package org.swyp.dessertbee.community.mate.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.mate.entity.MateReply;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateReplyRepository extends JpaRepository<MateReply, Long> {

    /**
     * 디저트메이트 댓글 전체 조회(삭제된 댓글 제외)
     * */
    @Query("SELECT m FROM MateReply m WHERE m.deletedAt IS NULL AND m.mateId = :mateId")
    Page<MateReply> findAllByDeletedAtIsNull(@Param("mateId") Long mateId, Pageable pageable);


    Optional<MateReply> findByMateIdAndMateReplyIdAndDeletedAtIsNull(Long mateId, Long replyId);

    Optional<MateReply> findByMateReplyIdAndDeletedAtIsNull(Long mateReplyId);

    List<MateReply> findByParentMateReplyIdAndDeletedAtIsNull(Long mateReplyId);
}
