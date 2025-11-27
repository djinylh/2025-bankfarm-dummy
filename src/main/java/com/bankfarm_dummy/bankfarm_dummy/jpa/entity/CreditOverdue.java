package com.bankfarm_dummy.bankfarm_dummy.jpa.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_overdue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditOverdue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_overdue_id", nullable = false)
    private Long cardOverdueId; // 연체 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_billing_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_credit_overdue_billing"))
    private CardBilling cardBilling; // 청구서 ID (FK)

    @Column(name = "card_overdue_amt", nullable = false)
    private Long cardOverdueAmt; // 연체 금액

    @Column(name = "card_st_at", nullable = false)
    private LocalDateTime cardStAt; // 연체 시작일

    @Column(name = "card_ed_at")
    private LocalDateTime cardEdAt; // 최종 납부일

    @Column(name = "card_overdue_pay_yn", nullable = false, length = 1)
    private String cardOverduePayYn; // 납입 여부 (Y/N)
}