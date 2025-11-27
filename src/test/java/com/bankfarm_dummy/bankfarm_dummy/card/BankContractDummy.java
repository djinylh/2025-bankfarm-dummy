package com.bankfarm_dummy.bankfarm_dummy.card;


import com.bankfarm_dummy.bankfarm_dummy.JpaDummy;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.Account;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.CheckCard;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.ProdDocument;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.UserCard;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card.ProdDocumentRepository;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card.UserCardRepository;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.employees.EmployeesRepository;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankContractDummy  extends JpaDummy {

    @Autowired
    UserCardRepository userCardRepository;

    @Autowired
    ProdDocumentRepository prodDocumentRepository;

    List<UserCard> userCardList;

    @BeforeAll
    void beforeAll() {
//        userCardList = userCardRepository.findAll();
    }

    @Test
    @Rollback(false)
    @Transactional()
    void insContract() {

        int pageSize = 1000;
        int page = 0;

        long totalCount = userCardRepository.count();   // 전체건수 (진행률 계산용)
        long processed = 0L;                            // 실제로 처리한 건수
        long maxProcess = totalCount;                        // 👈 여기까지만 처리하고 멈출 개수

        while (processed < maxProcess) {

            Page<UserCard> userCardPage = userCardRepository.findAll(
                    PageRequest.of(page, pageSize)
            );

            if (!userCardPage.hasContent()) {
                break; // 더 이상 데이터 없으면 종료
            }

            List<ProdDocument> prodDocuments = new ArrayList<>();

            for (UserCard uc : userCardPage.getContent()) {
                long brId = uc.getEmployee().getBranId();
                long docProdId = uc.getCardUserId();
                String docNm = "사용자보유카드 문서 이름";
                String docTp = "PD008";
                LocalDateTime time = LocalDateTime.now();

                ProdDocument prodDocument = generateProdDocument(brId, docProdId, docNm, docTp, time);
                prodDocuments.add(prodDocument);
            }

            prodDocumentRepository.saveAll(prodDocuments);

            // 이번 페이지에서 처리한 개수
            int current = userCardPage.getNumberOfElements();
            processed += current;   // 👈 누적 처리건수 증가

            double progress = (double) processed / totalCount * 100.0;
            System.out.println(
                    String.format("진행률: %d / %d (%.2f%%)", processed, totalCount, progress)
            );

            // 👉 maxProcess(예: 1000건)에 도달하면 바로 종료
            if (processed >= maxProcess) {
                System.out.println("✅ 최대 처리 개수 도달: " + maxProcess);
                break;
            }

            page++; // 다음 페이지
        }

        System.out.println("✅ 계약서 생성 완료");

    }

    ProdDocument generateProdDocument(long brId,long docProcId,String docNm,String docTp,LocalDateTime time) {
        return ProdDocument.builder()
                .branId(brId)
                .docProdId(docProcId)
                .docNm(docNm)
                .docProdTp(docTp)
                .docCrtAt(time)
                .build();


    }








}
