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

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 45)
    private String grade;

    @Column(name = "approval_yn")
    private  Boolean approvalYn;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
