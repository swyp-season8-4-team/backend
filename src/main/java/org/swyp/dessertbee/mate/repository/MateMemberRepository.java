package org.swyp.dessertbee.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.mate.entity.MateMember;

import java.util.List;

@Repository
public interface MateMemberRepository extends JpaRepository<MateMember, Long> {


    List<MateMember> findAllByMateId(Long mateId);
}
