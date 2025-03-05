package org.swyp.dessertbee.community.mate.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.swyp.dessertbee.common.entity.ReportCategory;

import java.security.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name="mate_report")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateReport {

    @Id
    @Column(name = "mate_report_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mateReportId;

    @Column(name = "report_category_id")
    private Long reportCategoryId;

    @Column(name = "mate_id", nullable = true)
    private Long mateId;

    @Column(name = "mate_reply_id", nullable = true)
    private Long mateReplyId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "comment")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;


}
