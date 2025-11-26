package com.bankfarm_dummy.bankfarm_dummy.card;

import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.*;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service  // ✅ 이거 필수! Bean으로 등록됨
public class CardBatchService {

    private final UserCardRepository userCardRepository;
    private final CheckCardRepository checkCardRepository;
    private final CreditCardRepository creditCardRepository;

    @PersistenceContext
    private EntityManager em;

    public CardBatchService(UserCardRepository userCardRepository,
                            CheckCardRepository checkCardRepository,
                            CreditCardRepository creditCardRepository) {
        this.userCardRepository = userCardRepository;
        this.checkCardRepository = checkCardRepository;
        this.creditCardRepository = creditCardRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void saveBatch(List<UserCard> ucList, List<CheckCard> chkList, List<CreditCard> crdList, List<Account> shuffledAccounts) {
        userCardRepository.saveAll(ucList);
        userCardRepository.flush();

//        int usedIdx = 0;
//        for (UserCard uc : ucList) {
//            if (uc.getCard().getCardTp() == 1) {
//                UserCard managedUc = em.merge(uc);
//                Account a = shuffledAccounts.get(usedIdx++);
//                CheckCard cc = CheckCard.builder()
//                        .userCard(managedUc)
//                        .account(a)
//                        .build();
//                chkList.add(cc);
//            }
//        }
//
//        if (!chkList.isEmpty()) {
//            checkCardRepository.saveAll(chkList);
//            checkCardRepository.flush();
//        }
//
//        if (!crdList.isEmpty()) {
//            creditCardRepository.saveAll(crdList);
//            creditCardRepository.flush();
//        }

        System.out.printf("💾 saveBatch 호출: uc=%d, chk=%d, crd=%d%n",
                ucList.size(), chkList.size(), crdList.size());

        em.clear();
    }
}