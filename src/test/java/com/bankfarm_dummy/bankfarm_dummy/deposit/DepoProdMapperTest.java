package com.bankfarm_dummy.bankfarm_dummy.deposit;

import com.bankfarm_dummy.bankfarm_dummy.Dummy;
import com.bankfarm_dummy.bankfarm_dummy.depo.common.DepoProdMapper;
import com.bankfarm_dummy.bankfarm_dummy.insurance.model.InsrProdReq;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class DepoProdMapperTest extends Dummy {
  final int ADD_ROW_COUNT = 300;

  @Test
  void insertDepoProd() {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    DepoProdMapper depoProdMapper = sqlSession.getMapper(DepoProdMapper.class);

    Random rnd = new Random();

    // 상품 타입 코드
    String[] typeCodes = {
        "DO002", // 정기 예금
        "DO003", // 정기 적금
        "DO004"  // 자유 적금
    };

    // 타입 코드랑 매핑할 상품 이름
    String[] typeNameKor = {
        "예금",
        "적금",
        "자유 적금"
    };

    // 앞, 가운데에 붙일 수식어
    String[] prefix = {
        "스마트", "하이", "플러스", "알뜰", "프리",
        "청년", "직장인", "아이꿈", "미래", "내일",
        "새싹", "씨앗", "모아", "든든", "행복",
        "골드", "프라임", "스타", "라이트", "에코",
        "디지털", "e-", "온", "마이", "콤보"
    };
    String[] middle = {
        "모아모아", "더하기", "플랜", "챌린지", "드림",
        "점프업", "부스트", "스페셜", "프리미엄", "클래식",
        "세이브", "기쁨", "스텝", "패키지", "온리",
        "베스트", "스마트", "케어", "에디션", "라운지",
        "스마일", "위드유", "투게더", "스토리", "플러스"
    };

    for (int i = 0; i < ADD_ROW_COUNT; i++) {

      // 타입 하나 뽑기
      int tpIdx = rnd.nextInt(typeCodes.length);
      String prodTpCode = typeCodes[tpIdx];
      String baseTypeNm = typeNameKor[tpIdx];

      // 상품 이름: 수식어 + 타입명 조합
      String prodNm =
          prefix[rnd.nextInt(prefix.length)] + " " +
              middle[rnd.nextInt(middle.length)] + " " +
              baseTypeNm;

      // 판매 시작일: 2020~2024 사이 랜덤
      LocalDate openDt = LocalDate.of(2020, 1, 1)
          .plusDays(rnd.nextInt(365 * 5));

      // 종료일: 어떤 건 null, 어떤 건 시작일 기준 몇 년 뒤
      LocalDate closeDt = null;
      if (rnd.nextBoolean()) {
        closeDt = openDt.plusYears(1 + rnd.nextInt(4));
      }

      // 수수료: 0.50 ~ 5.00% 이런 느낌
      BigDecimal commission = BigDecimal
          .valueOf(0.5 + rnd.nextDouble(4.5))
          .setScale(2, RoundingMode.HALF_UP);

      // 판매 여부: 종료일이 없거나 오늘 이후면 Y, 아니면 N
      String saleYn;
      if (closeDt == null || closeDt.isAfter(LocalDate.now())) {
        saleYn = "Y";
      } else {
        saleYn = "N";
      }

      InsrProdReq req = new InsrProdReq();
      req.setPartId(partId);
      req.setInsrCd(prodCd);
      req.setInsrNm(prodNm);
      req.setInsrProdTp(prodTpCode);
      req.setInsrOpenDt(openDt);
      req.setInsrCloseDt(closeDt);
      req.setInsrCommission(commission);
      req.setInsrSaleYn(saleYn);

      insrMapper.insrProdInsert(req);

      if (i > 0 && i % 200 == 0) {
        sqlSession.flushStatements();
      }
    }

    sqlSession.commit();
    sqlSession.close();
  }
}
