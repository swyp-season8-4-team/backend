package org.swyp.dessertbee.mate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mate_reply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MateReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mate_reply_id")
    private Long mateReplyId;

    @Column(name = "mate_id")
    private Long mateId;    //메이트 테이블 조인하기 위해 필요(mate 테이블 고유 id)

    @Column(name = "user_id")
    private Long userId;

    @Column
    private String content;


    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column
    private String report;
}
