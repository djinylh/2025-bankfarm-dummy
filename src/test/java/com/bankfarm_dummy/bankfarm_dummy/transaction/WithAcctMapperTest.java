package com.bankfarm_dummy.bankfarm_dummy.transaction;

import com.bankfarm_dummy.bankfarm_dummy.Dummy;
import com.bankfarm_dummy.bankfarm_dummy.account.AccountMapper;
import com.bankfarm_dummy.bankfarm_dummy.account.model.GetAcctBalRes;
import com.bankfarm_dummy.bankfarm_dummy.account.model.ModifyAcctBalByAcctIdReq;
import com.bankfarm_dummy.bankfarm_dummy.transaction.common.TransactionInsertGeq;
import com.bankfarm_dummy.bankfarm_dummy.transaction.common.TransactionMapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.Random;

class WithAcctMapperTest extends Dummy {

    // 총 계좌..
    private int ACCOUNT_ID = 300500;
    @Test
    void transactionInsert() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        AccountMapper accountMapper = sqlSession.getMapper(AccountMapper.class);
        TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);


        for(int i=1;i<=ACCOUNT_ID;i++){

            long acctId = i;

            // 계좌의 PK와 잔액 찾아오기
            GetAcctBalRes acctInfo = accountMapper.getAcctBalByAcctId(acctId);


            // 어떤 입출금 찍어낼지.
            int random = (int)(Math.random()*4);
//            int random = 3;
            // 입출금 내역 찍기
            TransactionInsertGeq req = new TransactionInsertGeq();

            // 계좌에서 보유 잔액 수정하기
            ModifyAcctBalByAcctIdReq req1 = new ModifyAcctBalByAcctIdReq();

            if (random == 0) {
                // 입출금 내역 찍기 ATM 출금
                long[] randomBal = {100000, 300000, 50000, 30000};
                int balInx = (int) (Math.random() * randomBal.length);


                req.setAcctId(acctId);
                req.setTrnsFeeId(2);
                req.setTrnsAmt(randomBal[balInx] + 1000);
                req.setTrnsBal(acctInfo.getAcctBal() - req.getTrnsAmt());
                req.setTrnsTp((byte) 2);
                req.setTrnsDes("ATM 출금");

                transactionMapper.transactionInsert(req);
                req1.setAcctId(acctId);
                req1.setAcctBal(req.getTrnsBal());
                accountMapper.ModifyAcctBalByAcctId(req1);
                sqlSession.flushStatements();
            }

            if (random == 1) {
                // 입출금 내역 찍기 ATM 입금
                long[] randomBal = {200000, 500000, 20000, 70000};
                int balInx = (int) (Math.random() * randomBal.length);

                req.setAcctId(acctId);
                req.setTrnsFeeId(2);
                req.setTrnsAmt(randomBal[balInx] - 1000);
                req.setTrnsBal(acctInfo.getAcctBal() + req.getTrnsAmt());
                req.setTrnsTp((byte) 1);
                req.setTrnsDes("ATM 입금");

                transactionMapper.transactionInsert(req);
                req1.setAcctId(acctId);
                req1.setAcctBal(req.getTrnsBal());
                accountMapper.ModifyAcctBalByAcctId(req1);
                sqlSession.flushStatements();
            }


            if (random == 2) {
                //남의 계좌 입금해주기
                long[] randomBal = {100000, 300000, 50000, 30000};
                int balInx = (int) (Math.random() * randomBal.length);
                req.setAcctId(acctId);
                req.setTrnsFeeId(2);
                req.setTrnsAmt(randomBal[balInx] + 1000);
                req.setTrnsBal(acctInfo.getAcctBal() - req.getTrnsAmt());
                req.setTrnsTp((byte) 3);
                req.setTrnsAcctNum(accountNum());
                req.setTrnsDes("계좌이체(출금)");

                transactionMapper.transactionInsert(req);
                req1.setAcctId(acctId);
                req1.setAcctBal(req.getTrnsBal());
                accountMapper.ModifyAcctBalByAcctId(req1);
                sqlSession.flushStatements();
            }
            if (random == 3) {
                //남의 계좌에서 입금받음
                long[] randomBal = {200000, 130000, 20000, 40000};
                int balInx = (int) (Math.random() * randomBal.length);
                req.setAcctId(acctId);
                req.setTrnsFeeId(1);
                req.setTrnsAmt(randomBal[balInx] );
                req.setTrnsBal(acctInfo.getAcctBal() + req.getTrnsAmt());
                req.setTrnsTp((byte) 3);
                req.setTrnsAcctNum(accountNum());
                req.setTrnsDes("계좌이체(입금)");

                transactionMapper.transactionInsert(req);
                req1.setAcctId(acctId);
                req1.setAcctBal(req.getTrnsBal());
                accountMapper.ModifyAcctBalByAcctId(req1);
                sqlSession.flushStatements();
            }


        }
        sqlSession.close();

    }

    private String accountNum() {
        Random randomAcct = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            sb.append(randomAcct.nextInt(10));
            if (i == 2 || i == 5) {
                sb.append("-");
            }
        }

        return sb.toString();


    }
}


