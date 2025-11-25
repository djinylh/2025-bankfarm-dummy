package com.bankfarm_dummy.bankfarm_dummy.account;

import com.bankfarm_dummy.bankfarm_dummy.account.model.*;

import java.util.List;

public interface AccountMapper {
    // 계좌 테이블 인서트 메소드
    int accountInsert(AccountInsertReq accountReq);
    // 계좌 번호 중복 체크용 메소드
    AccountFindAcctNumRes countByAccountNum(AccountFindAcctNumReq  accountFindAcctNumReq);
    List<GetDemandProdRes> prodByDemand();
    // 계좌 PK와 보유 금액 들고오기
    GetAcctBalRes getAcctBalByAcctId(long acctId);
    // 보유 금액 수정하기
    int ModifyAcctBalByAcctId(ModifyAcctBalByAcctIdReq req);

}
