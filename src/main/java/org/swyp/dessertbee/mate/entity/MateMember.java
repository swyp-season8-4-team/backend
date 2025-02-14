package org.swyp.dessertbee.mate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mate_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MateMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mate_member_id")
    private Long mateMemberId;  //메이트 멤버 고유 id

    @Column(name = "mate_id")
    private Long mateId;    //메이트 테이블 조인하기 위해 필요(mate 테이블 고유 id)

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MateMemberGrade grade;

    @Column(name = "approval_yn")
    private  Boolean approvalYn;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
