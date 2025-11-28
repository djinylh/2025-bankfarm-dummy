package com.bankfarm_dummy.bankfarm_dummy.card;
import com.bankfarm_dummy.bankfarm_dummy.JpaDummy;
import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.*;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.account.AccountRepository;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card.CardRepository;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.customer.CustomerRepository2;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.employees.EmployeesRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankUserCardDummy extends JpaDummy {

    @Autowired CustomerRepository2 customerRepository2;
    @Autowired AccountRepository accountRepository;
    @Autowired CardRepository cardRepository;
    @Autowired EmployeesRepository employeesRepository;
    @Autowired CardBatchService cardBatchService; // ✅ 새 서비스 주입

    @PersistenceContext
    private EntityManager em;

    List<Customer2> customerList2;
    List<Account> shuffledAccounts;
    List<Card> cardList;
    List<Employees> employeeList;

    @BeforeAll
    void beforeAll() {
        customerList2 = customerRepository2.findAll();
        cardList = cardRepository.findAll();
        employeeList = employeesRepository.findAll();
    }

    @Test
    @Rollback(false)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void insCards() {
        int SIZE = 100;
        int BATCH_SIZE = 20;
        int empIdx = 0;

        // 페이징으로 계좌 읽기
        List<Account> allAccounts = new ArrayList<>();
        int page = 0;
        int pageSize = 10000;
        Page<Account> pageResult;
        do {
            pageResult = accountRepository.findAll(PageRequest.of(page, pageSize));
            allAccounts.addAll(pageResult.getContent());
            em.clear();
            page++;
        } while (!pageResult.isLast());

        shuffledAccounts = allAccounts;

        List<UserCard> ucList = new ArrayList<>();
        List<CreditCard> creditCardList = new ArrayList<>();
        List<CheckCard> chkList = new ArrayList<>();
        List<Customer2> shuffledList = new ArrayList<>(customerList2);
        Collections.shuffle(shuffledList);

        for (int i = 0; i < SIZE; i++) {
            Customer2 c = shuffledList.get(i % shuffledList.size());
            Employees assignedEmp = employeeList.get(empIdx);
            Card assignedCard = cardList.get(faker.random().nextInt(cardList.size()));

            UserCard uc = generateUserCard(c, assignedCard, assignedEmp);
            ucList.add(uc);

            if (assignedCard.getCardTp() == 0) {
                creditCardList.add(generateCreditCard(uc));
            }

            empIdx = (empIdx + 1) % employeeList.size();

            if (ucList.size() % BATCH_SIZE == 0) {
                cardBatchService.saveBatch(ucList, chkList, creditCardList, shuffledAccounts); // ✅ 서비스 호출
                ucList.clear();
                chkList.clear();
                creditCardList.clear();
            }
        }

        if (!ucList.isEmpty()) {
            cardBatchService.saveBatch(ucList, chkList, creditCardList, shuffledAccounts);
        }
    }

    // 이하 generate 메서드 그대로 유지 (생략)

    void insCreditCard(){
        List<Account> allAccounts = new ArrayList<>();
        List<UserCard> allUserCards = new ArrayList<>();
        int page = 0;
        int pageSize = 5000;
        Page<Account> pageResult;
        do {
            pageResult = accountRepository.findAll(PageRequest.of(page, pageSize));
            allAccounts.addAll(pageResult.getContent());
            em.clear();
            page++;
        } while (!pageResult.isLast());
    }




    CheckCard generateCheckCard(UserCard uc, Account a) {
        return CheckCard.builder()
                .userCard(uc)  // ✅ 반드시 세팅해야 MapsId 작동
                .account(a)
                .build();
    }


    CreditCard generateCreditCard(UserCard uc) {
        return CreditCard.builder()
                .userCard(uc)
                .cardAcctId(generateAccountNo())
                .cardDueDay(generateDueDay())
                .cardBankCode(faker.options().option("BK001","BK002","BK003","BK004","BK005",
                        "BK006","BK007","BK008","BK009","BK010",
                        "BK011","BK012","BK013","BK014","BK015",
                        "BK016","BK017","BK018","BK019","BK020"))
                .build();
    }


    UserCard generateUserCard(Customer2 customer2,
                              Card card,
                              Employees employees) {
        return UserCard.builder()
                .card(card)
                .employee(employees)
                .customer2(customer2)
                .cardNum(generateCardNo())
                .cardSts(faker.options().option("CD006","CD007","CD008","CD009","CD010"))
                .cardDayLimit(faker.options().option(300000,500000,1000000,1500000))
                .cardMonthLimit(faker.options().option(1000000,1500000,2000000,3000000))
                .cardEdAt(randomDateFuture())
                .cardDeacAt(null)
                .build();


    }


    private static String generateCardNo() {
        return faker.number().digits(16);
    }

    private static String generateAccountNo() {
        return faker.number().digits(14);
    }


    private static byte generateDueDay() {
        return (byte) faker.number().numberBetween(1, 31);
    }

    private static LocalDateTime randomDateFuture() {
        return LocalDateTime.now().plusDays(
                ThreadLocalRandom.current().nextInt(30, 3000)
        );
    }








}
