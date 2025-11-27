package com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card;

import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CreditCardStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CreditCardStatementRepository extends JpaRepository<CreditCardStatement, String> {

    @Query("""
    SELECT cs
    FROM CreditCardStatement cs
    WHERE cs.cardCrdRefundYn = 'N'
      AND (cs.cardInstallments = 1
           OR EXISTS (
               SELECT 1 FROM CardInstallmentSchedule s
               WHERE s.creditCardStatement = cs
                 AND s.cardScheduleRefundYn = 'N'
           )
      )
""")
    Page<CreditCardStatement> findActiveStatements(Pageable pageable);

}
