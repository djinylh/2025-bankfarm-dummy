package com.bankfarm_dummy.bankfarm_dummy.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "overdue_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverdueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "od_id", nullable = false)
    private Long odId; // 연체 ID

    @Column(name = "cust_id", nullable = false)
    private Long custId; // 고객 ID

    @Column(name = "od_source_id", nullable = false)
    private Long odSourceId; // 해당 ID (연체 관련 원본 ID)

    @Column(name = "od_tp", nullable = false, length = 5)
    private String odTp; // 연체 타입 (카드/대출/보험 등)

    @Column(name = "od_st_dt", nullable = false)
    private LocalDate odStDt; // 연체 시작일

    @Column(name = "od_amt", nullable = false)
    private Long odAmt; // 연체 금액
}