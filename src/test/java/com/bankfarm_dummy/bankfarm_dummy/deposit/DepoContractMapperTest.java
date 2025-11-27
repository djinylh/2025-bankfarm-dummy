package com.bankfarm_dummy.bankfarm_dummy.deposit;

import com.bankfarm_dummy.bankfarm_dummy.Dummy;
import com.bankfarm_dummy.bankfarm_dummy.account.AccountInsertReq;
import com.bankfarm_dummy.bankfarm_dummy.account.AccountMapper;
import com.bankfarm_dummy.bankfarm_dummy.account.model.AccountFindAcctNumReq;
import com.bankfarm_dummy.bankfarm_dummy.account.model.AccountFindAcctNumRes;
import com.bankfarm_dummy.bankfarm_dummy.branch.BranchMapper;
import com.bankfarm_dummy.bankfarm_dummy.depo.*;
import com.bankfarm_dummy.bankfarm_dummy.depo.common.DepoContractInsertReq;
import com.bankfarm_dummy.bankfarm_dummy.depo.common.DepoContractMapper;
import com.bankfarm_dummy.bankfarm_dummy.depo.common.DepoProdMapper;
import com.bankfarm_dummy.bankfarm_dummy.prod_document.ProdDocumentMapper;
import com.bankfarm_dummy.bankfarm_dummy.prod_document.model.ProdDocumentReq;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DepoContractMapperTest extends Dummy {
  final int ADD_ROW_COUNT = 300;
  @Test
  void insertDepositContract() {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    AccountMapper accountMapper = sqlSession.getMapper(AccountMapper.class);
    DepoContractMapper depoContractMapper = sqlSession.getMapper(DepoContractMapper.class);
    ProdDocumentMapper prodDocumentMapper = sqlSession.getMapper(ProdDocumentMapper.class);

    for (int i = 0; i < ADD_ROW_COUNT; i++) {
      Random random = new Random();

      // fk로 쓸 리스트
      List<Long> custIds = depoContractMapper.selectCustomerIds();
      List<Long> empIds = depoContractMapper.selectEmployeeIds();
      List<DepoProdSelectRes> prodIds = depoContractMapper.selectDepoProds();

      // 랜덤으로 뽑은 fk 아이디, 상품 정보
      Long custId =  custIds.get(random.nextInt(custIds.size()));
      Long empId = empIds.get(random.nextInt(empIds.size()));
      DepoProdSelectRes prod = prodIds.get(random.nextInt(prodIds.size()));

      // 1. 계좌 생성
      // 계좌 번호 생성
      String finalAcctNum = accountNum(accountMapper);

      // 비밀번호 암호화
      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      String password = passwordEncoder.encode("1234");



      // 생성 일시(예적금 상품의 판매 시작일 ~ 오늘 사이의 날짜로)
      LocalDateTime start = LocalDateTime.of(prod.getDepoStartDt().getYear(), prod.getDepoStartDt().getMonth(), prod.getDepoStartDt().getDayOfMonth(), 0, 0, 0);
      LocalDateTime end = LocalDateTime.of(2025, 11, 26, 23, 59, 59);
      // 두 LocalDateTime 사이의 초(second) 단위 차이
      long seconds = java.time.Duration.between(start, end).getSeconds();
      // 0 ~ seconds 사이의 랜덤 초 생성
      long randomSeconds = (long) (Math.random() * seconds);
      // 시작 시간에서 랜덤 초를 더함
      LocalDateTime randomDateTime = start.plusSeconds(randomSeconds);
      // 예적금 계약에 넣을 날짜 타입으로 변환
      LocalDate randomDate = randomDateTime.toLocalDate();

      // 상품 만기일 계산(계좌 상태에 반영하기 위함)
      LocalDate maturityDate = randomDate.plusMonths(prod.getDepoTermMonth());
      // 계좌 상태
      LocalDate today = LocalDate.now();
      String acctSts = maturityDate.isAfter(today) ? "AS001" : "AS002";

      AccountInsertReq accountReq = new AccountInsertReq();
      accountReq.setCustId(custId);
      accountReq.setAccTp((byte)0);
      accountReq.setAcctSavTp("AC003");
      accountReq.setAcctNum(finalAcctNum);
      accountReq.setAcctPw(password);
      accountReq.setAcctBal(0);
      accountReq.setAcctDayLimit(0);
      accountReq.setAcctStsCd(acctSts);
      accountReq.setAcctCrtAt(randomDateTime);
      accountReq.setAcctIsDedYn('N');

      accountMapper.accountInsert(accountReq);

      sqlSession.flushStatements();

      // 2. 계약 공통 데이터 생성
      // 만기 일자


      // 지급 방식
      String[] payoutCodes = {"DO031", "DO032", "DO032", "DO032", "DO032"};
      String payoutTp = payoutCodes[(int)(Math.random() * payoutCodes.length)];

      // 지급 방식이 계좌 이체일 시 지급 은행, 계좌 번호 생성
      String payoutAcctNum = null;
      String payoutBank = null;
      if(payoutTp.equals("DO032")){
        payoutAcctNum = accountNum();

        String[] bankCodes = {
                "BK001","BK002","BK003","BK004","BK005",
                "BK006","BK007","BK008","BK009","BK010",
                "BK011","BK012","BK013","BK014","BK015",
                "BK016","BK017","BK018","BK019","BK020"
        };
        payoutBank = bankCodes[(int)(Math.random() * bankCodes.length)];
      }

      // 적용 금리(기본금리 ~ 기본금리 + 우대 금리)
      BigDecimal maxRate = depoContractMapper.selectProdTotalRate(prod.getDepoProdId());
      BigDecimal minRate = new BigDecimal("1.8500");

      BigDecimal randomRate = randomDecimal(minRate, maxRate);

      // 계약 상태
      String activeCd = accountReq.getAcctStsCd().equals("AS001") ? "CS001" : "CS002";

      DepoContractInsertReq depoReq = new DepoContractInsertReq();
      depoReq.setCustId(custId);
      depoReq.setDepoProdId(prod.getDepoProdId());
      depoReq.setAcctId(accountReq.getAcctId());
      depoReq.setEmpId(empId);
      depoReq.setDepoContractDt(randomDate);
      depoReq.setDepoMaturityDt(maturityDate);
      depoReq.setDepoAppliedIntrstRt(randomRate);
      depoReq.setDepoActiveCd(activeCd);
      depoReq.setDepoPayoutBankCd(payoutBank);
      depoReq.setDepoPayoutAcctNum(payoutAcctNum);
      depoReq.setDepoPayoutTp(payoutTp);

      depoContractMapper.depoContractInsert(depoReq);

      sqlSession.flushStatements();

      // 3. 계약 상세 데이터 / 정기적금 납입 스케줄 생성
      int minAmt = prod.getDepoMinAmt();
      int maxAmt = 0;
      Long randomAmt = null;

      if(prod.getDepoProdTp().equals("DO002")){
        maxAmt = 1000000000;
        randomAmt = ThreadLocalRandom.current().nextLong(minAmt, maxAmt + 1);

        // 정기 예금 상세 계약 데이터
        DepoContractDeposit depositReq = new DepoContractDeposit();
        depositReq.setDepoContractId(depoReq.getDepoContractId());
        depositReq.setDepoPrncpAmt(randomAmt);

        depoContractMapper.insertDepoContractDeposit(depositReq);
      }else if(prod.getDepoProdTp().equals("DO003")){
        maxAmt = prod.getDepoMaxAmt();
        randomAmt = ThreadLocalRandom.current().nextLong(minAmt, maxAmt + 1);

        // 납입일(1~28)
        byte paymentDay = (byte) (1 + random.nextInt(28));

        // 정기 적금 상세 계약 데이터
        DepoContractSavings savingsReq = new DepoContractSavings();
        savingsReq.setDepoContractId(depoReq.getDepoContractId());
        savingsReq.setDepoPaymentDay(paymentDay);
        savingsReq.setDepoMonthlyAmt(randomAmt);

        depoContractMapper.insertDepoContractSavings(savingsReq);

        // 정기적금 납입 내역 데이터
        LocalDate paymentDate = depoReq.getDepoContractDt();
        while(!paymentDate.isAfter(today)){
          paymentDate = paymentDate.plusMonths(1);

          DepoSavingsPaymentReq savingsPaymentReq = new DepoSavingsPaymentReq();
          savingsPaymentReq.setDepoContractId(depoReq.getDepoContractId());
          savingsPaymentReq.setDepoPaidDt(paymentDate);
          savingsPaymentReq.setDepoPaidAmt(randomAmt);
          savingsPaymentReq.setDepoPaymentYn("Y");

          depoContractMapper.insertDepoSavingsPayment(savingsPaymentReq);

          if (paymentDate.isAfter(today)) break;
        }

        sqlSession.flushStatements();
      }else if(prod.getDepoProdTp().equals("DO004")){
        maxAmt = prod.getDepoMaxAmt();
        randomAmt = ThreadLocalRandom.current().nextLong(minAmt, maxAmt + 1);

        // 가입 날 최초 납입 한번
        DepoSavingsPaymentReq savingsPaymentReq = new DepoSavingsPaymentReq();
        savingsPaymentReq.setDepoContractId(depoReq.getDepoContractId());
        savingsPaymentReq.setDepoPaidDt(randomDate);
        savingsPaymentReq.setDepoPaidAmt(randomAmt);
        savingsPaymentReq.setDepoPaymentYn("Y");

        depoContractMapper.insertDepoSavingsPayment(savingsPaymentReq);

        LocalDate currentMonth = depoReq.getDepoContractDt().withDayOfMonth(1).plusMonths(1);

        while (!currentMonth.isAfter(today)) {

          // 이 달에 몇 번 납입할지 결정 (0~2회)
          int payCount = random.nextInt(4); // 0,1,2 중 하나

          // 같은 달 안에서 날짜가 겹치지 않도록 Set으로 관리
          Set<Integer> daySet = new HashSet<>();
          while (daySet.size() < payCount) {
            int day = 1 + random.nextInt(28); // 1~28일 랜덤
            daySet.add(day);
          }

          for (int day : daySet) {
            LocalDate paidDate = currentMonth.withDayOfMonth(day);

            // 계약 시작일 이전이거나 오늘 이후는 제외
            if (paidDate.isBefore(depoReq.getDepoContractDt()) || paidDate.isAfter(today)) {
              continue;
            }

            // 자유적금은 납입할 때마다 금액을 새로 랜덤
            randomAmt = ThreadLocalRandom.current().nextLong(minAmt, maxAmt + 1);

            DepoSavingsPaymentReq savingsPaymentReqSec = new DepoSavingsPaymentReq();
            savingsPaymentReqSec.setDepoContractId(depoReq.getDepoContractId());
            savingsPaymentReqSec.setDepoPaidDt(paidDate);
            savingsPaymentReqSec.setDepoPaidAmt(randomAmt);
            savingsPaymentReqSec.setDepoPaymentYn("Y");

            depoContractMapper.insertDepoSavingsPayment(savingsPaymentReqSec);
          }

          // 다음 달로 이동
          currentMonth = currentMonth.plusMonths(1);
        }
        sqlSession.flushStatements();
      }
      // 4. 계약서 보관 테이블 데이터
      Long branId = depoContractMapper.selectBranchIdByEmpId(empId);
      String docTitle = "";
      switch(prod.getDepoProdTp()){
        case "DO002":
          docTitle = "정기 예금 계약 문서 제목";
          break;
        case "DO003":
          docTitle = "정기 적금 계약 문서 제목";
          break;
        case "DO004":
          docTitle = "자유 적금 계약 문서 제목";
          break;
      }

      ProdDocumentReq documentReq = new ProdDocumentReq();
      documentReq.setBranId(branId);
      documentReq.setDocNm(docTitle);
      documentReq.setDocProdTp("PD006");
      documentReq.setDocProdId(depoReq.getDepoProdId());
      prodDocumentMapper.prodDocumentJoin(documentReq);
    }
    sqlSession.commit();
    sqlSession.close();
  }

  // 상품 금리 테이블 더미데이터
  @Test
  void insertDepoProdRate(){
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

    DepoContractMapper depoContractMapper = sqlSession.getMapper(DepoContractMapper.class);

    // 금리 적용할 예적금 상품 아이디 리스트 가져오기
    List<Long> prodIds = depoContractMapper.selectDepoProdIds();

    // 우대금리 아이디 리스트 가져오기
    List<Long> rateIds = depoContractMapper.selectRateRule();

    Random random = new Random();
    DepoProdRateReq depoProdRateReq = new DepoProdRateReq();

    for(int i=0;i<prodIds.size();i++){
      // 반복문 돌 때 마다 인덱스 순으로 순차 id뽑기
      Long prodId = prodIds.get(i);
      // 적용할 우대 금리 갯수 뽑기
      int repeatNum = random.nextInt(5) + 3;
      // 금리 아이디 배열 복사 후 섞기
      List<Long> result = new ArrayList<>(rateIds);
      Collections.shuffle(result);
      // 우대금리 갯수만큼 배열 자르기(랜덤 아이디 생성과 같은 방식)
      List<Long> picked = result.stream()
          .limit(repeatNum)
          .toList();

      for(int k = 0; k < picked.size(); k++){
        Long pickedId = picked.get(k);
        depoProdRateReq.setProdId(prodId);
        depoProdRateReq.setProdTp("RT006");
        depoProdRateReq.setProdRtId(pickedId);

        depoContractMapper.insertDepoProdRate(depoProdRateReq);
      }
    }
    sqlSession.commit();
    sqlSession.close();
  }
  private String accountNum(AccountMapper accountMapper){
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

    while(true){
      Random random = new Random();
      StringBuilder sb = new StringBuilder();
      for(int i= 0; i<14;i++){
        sb.append(random.nextInt(10));
        if(i==6 || i==8){
          sb.append("-");
        }
      }
      String accountNum = sb.toString();
      AccountFindAcctNumReq acctFindReq =  new AccountFindAcctNumReq();
      acctFindReq.setAcctNum(accountNum);
      AccountFindAcctNumRes y = accountMapper.countByAccountNum(acctFindReq);

      if(y==null){
        return accountNum;
      }
    }




  }
  // 랜덤 우대금리 만들기
  private BigDecimal randomDecimal(BigDecimal min, BigDecimal max) {
    Random random = new Random();

    // 0 이상 1 미만 난수
    double randomDouble = random.nextDouble();

    // (max - min) * 난수 + min
    BigDecimal result = max.subtract(min)
        .multiply(BigDecimal.valueOf(randomDouble))
        .add(min);

    // DECIMAL(6,4)에 맞게 소수점 4자리 고정
    return result.setScale(4, RoundingMode.HALF_UP);
  }
}
