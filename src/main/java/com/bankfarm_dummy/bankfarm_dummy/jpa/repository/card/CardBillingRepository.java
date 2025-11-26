package com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card;

import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CardBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CardBillingRepository extends JpaRepository<CardBilling, Long> {

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE card_billing b
            JOIN (
                SELECT
                    uc.card_user_id,
                    DATE_FORMAT(cs.card_trns_dt, '%Y-%m-01') AS billing_month,
            
                    SUM(
                        CASE
                            WHEN LOWER(TRIM(COALESCE(cs.card_crd_refund_yn, 'n'))) = 'n'
                                 AND cs.card_installments = 1
                            THEN cs.card_og_amt
                            ELSE 0
                        END
                    ) AS total_one_time,
            
                    SUM(
                        CASE
                            WHEN LOWER(TRIM(COALESCE(s.card_schedule_refund_yn, 'n'))) = 'n'
                                 AND cs.card_installments > 1
                            THEN COALESCE(s.card_installment_amt, 0)
                            ELSE 0
                        END
                    ) AS total_installments
            
                FROM credit_card_statement cs
                JOIN user_card uc ON uc.card_user_id = cs.card_user_id
                LEFT JOIN card_installment_schedule s ON s.card_crd_statement_id = cs.card_crd_statement_id
                GROUP BY uc.card_user_id, billing_month
            ) tmp
            ON b.card_user_id = tmp.card_user_id
            AND DATE(b.card_billing_year_month) = DATE(tmp.billing_month)
            SET
                b.card_new_charges = COALESCE(tmp.total_one_time, 0),
                b.card_installment_amt = COALESCE(tmp.total_installments, 0),
                b.card_total_due = COALESCE(tmp.total_one_time, 0) + COALESCE(tmp.total_installments, 0),
                b.card_billing_sts = 'CD027';
        """, nativeQuery = true)
    int bulkUpdateBilling();


    Optional<CardBilling> findByUserCard_CardUserIdAndCardBillingYearMonth(Long cardUserId, LocalDate ym);
}













