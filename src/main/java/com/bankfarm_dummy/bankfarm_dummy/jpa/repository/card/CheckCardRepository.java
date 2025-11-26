package com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card;

import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CheckCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface CheckCardRepository extends JpaRepository<CheckCard, String>{


    @Query("select distinct cc.account.acctId from CheckCard cc")
    Set<Long> findAllAccountIds();

    boolean existsByAccount_AcctId(Long acctId);
}
