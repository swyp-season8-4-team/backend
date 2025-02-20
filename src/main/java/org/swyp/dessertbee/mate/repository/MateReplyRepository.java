package org.swyp.dessertbee.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.mate.entity.MateReply;

@Repository
public interface MateReplyRepository extends JpaRepository<MateReply, Long> {
}
