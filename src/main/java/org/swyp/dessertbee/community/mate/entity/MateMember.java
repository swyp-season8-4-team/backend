package org.swyp.dessertbee.community.mate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.swyp.dessertbee.user.entity.UserEntity;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MateApplyStatus applyStatus;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void updateStatus(MateApplyStatus applyStatus) {
        this.applyStatus = applyStatus;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }

    public Boolean isBanned(){
        return applyStatus == MateApplyStatus.BANNED;
    }

    public Boolean isPending()
    {
        return this.applyStatus == MateApplyStatus.PENDING;
    }

    public Boolean isReject()
    {
        return this.applyStatus == MateApplyStatus.REJECTED;
    }

    public Boolean isReapply()
    {
        return this.applyStatus == MateApplyStatus.APPROVED && this.deletedAt != null;
    }

    public Boolean isApprove() {
        return this.applyStatus == MateApplyStatus.APPROVED && this.getDeletedAt() == null;
    }

}
