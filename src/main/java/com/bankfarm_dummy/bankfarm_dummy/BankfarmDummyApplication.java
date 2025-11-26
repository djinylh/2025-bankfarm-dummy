//package com.bankfarm_dummy.bankfarm_dummy;
//
//import com.bankfarm_dummy.bankfarm_dummy.card.BankUserCardDummyTest; // ✅ import 추가
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//
//@SpringBootApplication
//public class BankfarmDummyApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(BankfarmDummyApplication.class, args);
//    }
//
//    // ✅ Application 실행 시 BankUserCardDummyTest.insCards() 자동 실행
//    @Bean
//    CommandLineRunner runDummy(BankUserCardDummyTest dummy) {
//        return args -> {
//            System.out.println("🚀 더미데이터 생성 시작");
//            dummy.insCards(); // insCards() 실제 실행
//            System.out.println("✅ 더미데이터 생성 완료");
//        };
//    }
//}

package com.bankfarm_dummy.bankfarm_dummy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankfarmDummyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankfarmDummyApplication.class, args);
    }
}