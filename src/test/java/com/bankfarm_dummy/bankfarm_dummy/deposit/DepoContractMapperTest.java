package com.bankfarm_dummy.bankfarm_dummy.deposit;

import com.bankfarm_dummy.bankfarm_dummy.Dummy;
import com.bankfarm_dummy.bankfarm_dummy.account.AccountInsertReq;
import com.bankfarm_dummy.bankfarm_dummy.account.AccountMapper;
import com.bankfarm_dummy.bankfarm_dummy.account.model.AccountFindAcctNumReq;
import com.bankfarm_dummy.bankfarm_dummy.account.model.AccountFindAcctNumRes;
import com.bankfarm_dummy.bankfarm_dummy.account.model.GetDemandProdRes;
import com.bankfarm_dummy.bankfarm_dummy.branch.BranchMapper;
import com.bankfarm_dummy.bankfarm_dummy.branch.model.GetBranchByEmpRes;
import com.bankfarm_dummy.bankfarm_dummy.depo.DepoProdSelectRes;
import com.bankfarm_dummy.bankfarm_dummy.depo.common.DepoContractInsertReq;
import com.bankfarm_dummy.bankfarm_dummy.depo.common.DepoContractMapper;
import com.bankfarm_dummy.bankfarm_dummy.depo.common.DepoProdMapper;
import com.bankfarm_dummy.bankfarm_dummy.prod_document.ProdDocumentMapper;
import com.bankfarm_dummy.bankfarm_dummy.prod_document.model.ProdDocumentReq;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class DepoContractMapperTest extends Dummy {
  final int ADD_ROW_COUNT = 300;
  @Test
  void getAccountByID() {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    AccountMapper accountMapper = sqlSession.getMapper(AccountMapper.class);
    DepoContractMapper depoContractMapper = sqlSession.getMapper(DepoContractMapper.class);
    DepoProdMapper depoProdMapper = sqlSession.getMapper(DepoProdMapper.class);
    BranchMapper branchMapper = sqlSession.getMapper(BranchMapper.class);
    ProdDocumentMapper prodDocumentMapper = sqlSession.getMapper(ProdDocumentMapper.class);

    for (int i = 0; i < ADD_ROW_COUNT; i++) {
      Random random = new Random();

      // fk로 쓸 아이디 리스트
      List<Long> custIds = depoContractMapper.selectCustomerIds();
      List<Long> empIds = depoContractMapper.selectEmployeeIds();
      List<DepoProdSelectRes> prodIds = depoContractMapper.selectDepoProds();

      // 랜덤으로 뽑은 fk 아이디
      Long custId =  custIds.get(random.nextInt(custIds.size()));
      Long empId = empIds.get(random.nextInt(empIds.size()));
      DepoProdSelectRes prod = prodIds.get(random.nextInt(prodIds.size()));

      // 계좌 번호 생성
      String finalAcctNum = accountNum();

      // 비밀번호 암호화
      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      String password = passwordEncoder.encode("1234");

      // 계좌 상태
      String[] cd = {"AS001","AS002","AS005"};
      String acctSts = cd[(int)(Math.random()*cd.length)];

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

      // 계좌 생성
      accountMapper.accountInsert(accountReq);

      // 만기 일자
      LocalDate maturityDate = randomDate.plusMonths(prod.getDepoTermMonth());

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


      DepoContractInsertReq depoReq = new DepoContractInsertReq();
      depoReq.setCustId(custId);
      depoReq.setDepoProdId(prod.getDepoProdId());
      depoReq.setAcctId(accountReq.getAcctId());
      depoReq.setEmpId(empId);
      depoReq.setDepoContractDt(randomDate);
      depoReq.setDepoMaturityDt(maturityDate);
//      depoReq.setDepoAppliedIntrstRt();
      depoReq.setDepoActiveCd("CS001");
      depoReq.setDepoPayoutBankCd(payoutBank);
      depoReq.setDepoPayoutAcctNum(payoutAcctNum);
      depoReq.setDepoPayoutTp(payoutTp);
    }

      sqlSession.flushStatements();



      // 상품 계약 테이블 이동
//      GetBranchByEmpRes empRes = branchMapper.getBranchIdByEmpId(empId);
//      long branchId = empRes.getBranId();
//
//
//      ProdDocumentReq prodReq = new ProdDocumentReq();
//      prodReq.setBranId(branchId);
//      prodReq.setDocProdTp("PD006");
//      prodReq.setDocNm("요구불 계좌 문서 이름");
//      prodReq.setDocProdId(contReq.getDepoContractId());
//      prodDocumentMapper.prodDocumentJoin(prodReq);
//
//      sqlSession.flushStatements();
//
//    }
//    sqlSession.commit();
//    sqlSession.close();


  }


  private String accountNum(){
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

    //계좌 맵퍼
    AccountMapper accountMapper = sqlSession.getMapper(AccountMapper.class);
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
}
