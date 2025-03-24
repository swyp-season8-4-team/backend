package org.swyp.dessertbee.common.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "report_category")
public class ReportCategory {

    @Id
    @Column(name = "report_category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportCategoryId;


    @Column(name = "report_comment")
    private String reportComment;
}
