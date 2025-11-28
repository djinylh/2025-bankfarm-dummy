package com.bankfarm_dummy.bankfarm_dummy.card;

import com.bankfarm_dummy.bankfarm_dummy.JpaDummy;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CardBilling;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CreditCardStatement;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CreditOverdue;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.OverdueHistory;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankCardBillingDummy3 extends JpaDummy {

    @Autowired
    CardBillingRepository cardBillingRepository;
    @Autowired
    CreditOverdueRepository cardOverdueRepository;

    @Autowired
    OverdueHistoryRepository overdueHistoryRepository;


    @PersistenceContext
    private EntityManager em;

    @BeforeAll
    void beforeAll() {
        System.out.println("✅ 카드 명세서 데이터 페이징 로딩 준비 완료");
    }

    @Test
    @Transactional
    @Rollback(false)
    void run() {
//        payBillingDummy();
        checkAndRegisterOverdueBills();

    }

    void payBillingDummy() {
        System.out.println("\n💳 [STEP 3] 청구서 납부 처리 시작...");
        long startTime = System.currentTimeMillis();

        // 납부 대상: 미납 상태(CD027) 청구서 중 일부 샘플 처리
        List<CardBilling> unpaidList = cardBillingRepository
                .findByCardBillingStsAndCardBillingIdBetween("CD027", 1L, 99635L);

        int success = 0;
        int failed = 0;
        Random random = new Random();

        for (CardBilling billing : unpaidList) {
            try {
                payBilling(billing.getCardBillingId(), billing.getCardTotalDue(), random);
                success++;
            } catch (Exception e) {
                System.err.printf("❌ 청구서 %d 납부 실패: %s%n", billing.getCardBillingId(), e.getMessage());
                failed++;
            }
        }

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("✅ 납부 처리 완료 — 성공 %d건, 실패 %d건 (소요: %ds)%n", success, failed, elapsed);
    }

    /**
     * 💰 개별 청구서 납부 처리 (JPA로 구현한 PROC_PAY_CARD_BILLING)
     */
    private void payBilling(Long billingId, int payAmount, Random random) {
        Optional<CardBilling> optionalBilling = cardBillingRepository.findById(billingId);
        if (optionalBilling.isEmpty()) {
            throw new IllegalArgumentException("해당 청구서가 존재하지 않습니다. ID=" + billingId);
        }

        CardBilling billing = optionalBilling.get();
        if (random.nextBoolean()) {
            if (payAmount < billing.getCardTotalDue()) {
                throw new IllegalArgumentException("납부금액이 부족합니다. 필요금액: " + billing.getCardTotalDue());
            }

            billing.setCardPaidAmt(payAmount);
            billing.setCardBillingSts("CD028"); // 예: 납부 완료 코드
            //    billing.setCardPaymentDt(LocalDateTime.now());

            cardBillingRepository.save(billing);

            System.out.printf("💰 Billing ID %d → 납부 완료 (%d원)%n", billingId, payAmount);
        }
    }


    void checkAndRegisterOverdueBills() {
        System.out.println("\n⚠️ [STEP 4] 납부 마감일 경과 청구서 → 연체 처리 시작...");
        long start = System.currentTimeMillis();

        // [1] 마감일이 지났고 아직 납부 완료 안 된 청구서 조회
        List<CardBilling> overdueCandidates = cardBillingRepository.findAll().stream()
                .filter(b ->
                        b.getCardBillingSts().equals("CD027") && (b.getCardTotalDue() - b.getCardPaidAmt() != 0) && // 청구중
                                b.getCardDueDate() != null &&
                                b.getCardDueDate().isBefore(b.getCardDueDate().plusDays(ThreadLocalRandom.current().nextInt(1, 10)))
                )
                .toList();

        int total = overdueCandidates.size(); // 전체 개수
        int i = 0;
        int updated = 0;

// 💡 배치 insert용 리스트 준비
        List<CardBilling> billingToUpdate = new ArrayList<>();
        List<CreditOverdue> overdueToInsert = new ArrayList<>();
        List<OverdueHistory> historyToInsert = new ArrayList<>();

        for (CardBilling billing : overdueCandidates) {
            try {
                i++;

                // [1] 상태 변경
                billing.setCardBillingSts("CD029"); // 연체중
                billingToUpdate.add(billing);

                // [2] 연체 테이블 등록 (중복 방지)
                boolean exists = cardOverdueRepository.existsByCardBilling_CardBillingId(billing.getCardBillingId());
                if (!exists) {
                    CreditOverdue overdue = CreditOverdue.builder()
                            .cardBilling(billing)
                            .cardOverdueAmt((long) (billing.getCardTotalDue() - billing.getCardPaidAmt()))
                            .cardStAt(billing.getCardDueDate().plusDays(ThreadLocalRandom.current().nextInt(1, 10)))
                            .cardOverduePayYn("N")
                            .build();
                    overdueToInsert.add(overdue);

                    // [3] 연체 이력 등록
                    long custId = billing.getUserCard().getCustomer2().getCustId();
                    long billingId = billing.getCardBillingId();
                    String odTp = "OD002";
                    LocalDate od_st_dt = overdue.getCardStAt().toLocalDate();
                    long od_amt = billing.getCardTotalDue();

                    OverdueHistory overdueHistory = OverdueHistory.builder()
                            .custId(custId)
                            .odSourceId(billingId)
                            .odTp(odTp)
                            .odAmt(od_amt)
                            .odStDt(od_st_dt)
                            .build();
                    historyToInsert.add(overdueHistory);

                    System.out.printf("⚠️ 연체 등록됨: billing_id=%d, 금액=%d%n",
                            billing.getCardBillingId(), overdue.getCardOverdueAmt());
                } else {
                    System.out.printf("ℹ️ 이미 연체등록된 청구서: billing_id=%d%n", billing.getCardBillingId());
                }

                updated++;

                // ✅ 진행률 계산
                double progress = (i * 100.0) / total;
                System.out.printf("📊 진행률: %d/%d (%.2f%%)%n", i, total, progress);

                // ✅ 테스트용 임시 종료
                if (i == total) break;

            } catch (Exception e) {
                System.err.printf("❌ 처리 중 오류 (billing_id=%d): %s%n",
                        billing.getCardBillingId(), e.getMessage());
            }
        }

// 💾 [4] 한 번에 저장
        cardBillingRepository.saveAll(billingToUpdate);
        cardOverdueRepository.saveAll(overdueToInsert);
        overdueHistoryRepository.saveAll(historyToInsert);

        System.out.printf("✅ 총 %d건 중 %d건 연체처리 완료 (성공률 %.2f%%)%n",
                total, updated, (updated * 100.0 / total));
    }
}



