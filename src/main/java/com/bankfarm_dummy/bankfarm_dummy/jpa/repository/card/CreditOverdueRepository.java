package com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card;

import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CardBilling;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CreditOverdue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditOverdueRepository extends JpaRepository<CreditOverdue, String> {

    boolean existsByCardBilling_CardBillingId(Long cardBillingId);

    Optional<CreditOverdue> findByCardBilling(CardBilling cardBilling);
}
