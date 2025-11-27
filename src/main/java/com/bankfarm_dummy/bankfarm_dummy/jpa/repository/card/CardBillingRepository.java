package com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card;

import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CardBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardBillingRepository extends JpaRepository<CardBilling, Long> {

    List<CardBilling> findByCardBillingStsAndCardBillingIdBetween(
            String cardBillingSts, Long startId, Long endId
    );
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE card_billing b
            JOIN (
                SELECT
                    user_id,
                    billing_month,
                    SUM(one_time_amt) AS total_new_charges,
                    SUM(installment_amt) AS total_installments
                FROM (
                    -- 🧾 일시불 거래 (사전에 집계)
                    SELECT
                        cs.card_user_id AS user_id,
                        DATE_FORMAT(DATE_ADD(cs.card_trns_dt, INTERVAL 1 MONTH), '%Y-%m-01') AS billing_month,
                        SUM(cs.card_og_amt) AS one_time_amt,
                        0 AS installment_amt
                    FROM credit_card_statement cs
                    WHERE (cs.card_installments = 1 OR cs.card_installments IS NULL)
                      AND (cs.card_crd_refund_yn = 'N' OR cs.card_crd_refund_yn IS NULL)
                    GROUP BY cs.card_user_id, DATE_FORMAT(DATE_ADD(cs.card_trns_dt, INTERVAL 1 MONTH), '%Y-%m-01')
            
                    UNION ALL
            
                    -- 💳 할부 거래 (회차별 due_at 기준, 사전에 집계)
                    SELECT
                        cs.card_user_id AS user_id,
                        DATE_FORMAT(s.card_due_at, '%Y-%m-01') AS billing_month,
                        0 AS one_time_amt,
                        SUM(s.card_installment_amt) AS installment_amt
                    FROM credit_card_statement cs
                    JOIN card_installment_schedule s
                      ON s.card_crd_statement_id = cs.card_crd_statement_id
                    WHERE cs.card_installments > 1
                      AND (s.card_schedule_refund_yn = 'N' OR s.card_schedule_refund_yn IS NULL)
                    GROUP BY cs.card_user_id, DATE_FORMAT(s.card_due_at, '%Y-%m-01')
                ) t
                GROUP BY user_id, billing_month
            ) agg
              ON b.card_user_id = agg.user_id
             AND DATE_FORMAT(b.card_billing_year_month, '%Y-%m-01') = agg.billing_month
            SET
                b.card_new_charges = COALESCE(agg.total_new_charges, 0),
                b.card_installment_amt = COALESCE(agg.total_installments, 0),
                b.card_total_due = COALESCE(agg.total_new_charges, 0) + COALESCE(agg.total_installments, 0),
                b.card_billing_sts = 'CD027';
            WHERE b.card_billing_sts = 'CD026';
    """, nativeQuery = true)
    int updateBillingAmounts();

    Optional<CardBilling> findByUserCard_CardUserIdAndCardBillingYearMonth(Long cardUserId, LocalDate ym);
}













