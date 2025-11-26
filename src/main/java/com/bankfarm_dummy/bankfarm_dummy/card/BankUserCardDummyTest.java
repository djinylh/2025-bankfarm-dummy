package com.bankfarm_dummy.bankfarm_dummy.card;

import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.*;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.account.AccountRepository;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card.CardRepository;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.customer.CustomerRepository2;
import com.bankfarm_dummy.bankfarm_dummy.jpa.repository.employees.EmployeesRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component  // ✅ Spring Bean 등록
public class BankUserCardDummyTest {

    @Autowired CustomerRepository2 customerRepository2;
    @Autowired AccountRepository accountRepository;
    @Autowired CardRepository cardRepository;
    @Autowired EmployeesRepository employeesRepository;
    @Autowired CardBatchService cardBatchService;

    @PersistenceContext
    private EntityManager em;

    private List<Customer2> customerList2;
    private List<Card> cardList;
    private List<Employees> employeeList;
    private List<Account> shuffledAccounts;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void insCards() {
        System.out.println("🚀 BankUserCardDummyTest.insCards() 실행 시작");

        customerList2 = customerRepository2.findAll();
        cardList = cardRepository.findAll();
        employeeList = employeesRepository.findAll();

        // 계좌 목록 페이징 로드
        List<Account> allAccounts = new ArrayList<>();
        int page = 0;
        int pageSize = 10000;
        var pageResult = accountRepository.findAll(org.springframework.data.domain.PageRequest.of(page, pageSize));
        allAccounts.addAll(pageResult.getContent());
        shuffledAccounts = allAccounts;

        int SIZE = 100;
        int BATCH_SIZE = 20;
        int empIdx = 0;

        List<UserCard> ucList = new ArrayList<>();
        List<CreditCard> creditCardList = new ArrayList<>();
        List<CheckCard> chkList = new ArrayList<>();

        List<Customer2> shuffledList = new ArrayList<>(customerList2);
        Collections.shuffle(shuffledList);

        for (int i = 0; i < SIZE; i++) {
            Customer2 c = shuffledList.get(i % shuffledList.size());
            Employees assignedEmp = employeeList.get(empIdx);
            Card assignedCard = cardList.get(new Random().nextInt(cardList.size()));

            UserCard uc = generateUserCard(c, assignedCard, assignedEmp);
            ucList.add(uc);

            if (assignedCard.getCardTp() == 0) {
                creditCardList.add(generateCreditCard(uc));
            }

            empIdx = (empIdx + 1) % employeeList.size();

            if (ucList.size() % BATCH_SIZE == 0) {
                cardBatchService.saveBatch(ucList, chkList, creditCardList, shuffledAccounts);
                ucList.clear();
                chkList.clear();
                creditCardList.clear();
            }
        }

        if (!ucList.isEmpty()) {
            cardBatchService.saveBatch(ucList, chkList, creditCardList, shuffledAccounts);
        }

        System.out.println("✅ BankUserCardDummyTest.insCards() 완료");
    }

    // ------------------ Helper Methods ------------------
    CheckCard generateCheckCard(UserCard uc, Account a) {
        return CheckCard.builder().userCard(uc).account(a).build();
    }

    CreditCard generateCreditCard(UserCard uc) {
        return CreditCard.builder()
                .userCard(uc)
                .cardAcctId(generateAccountNo())
                .cardDueDay(generateDueDay())
                .cardBankCode("BK00" + (1 + new Random().nextInt(20)))
                .build();
    }

    UserCard generateUserCard(Customer2 customer2, Card card, Employees employees) {
        return UserCard.builder()
                .card(card)
                .employee(employees)
                .customer2(customer2)
                .cardNum(generateCardNo())
                .cardSts("CD00" + (6 + new Random().nextInt(5)))
                .cardDayLimit(500000)
                .cardMonthLimit(2000000)
                .cardEdAt(randomDateFuture())
                .build();
    }

    private static String generateCardNo() {
        return String.format("%016d", new Random().nextLong() & Long.MAX_VALUE);
    }

    private static String generateAccountNo() {
        return String.format("%014d", new Random().nextLong() & Long.MAX_VALUE);
    }

    private static byte generateDueDay() {
        return (byte) (1 + new Random().nextInt(30));
    }

    private static LocalDateTime randomDateFuture() {
        return LocalDateTime.now().plusDays(ThreadLocalRandom.current().nextInt(30, 3000));
    }
}