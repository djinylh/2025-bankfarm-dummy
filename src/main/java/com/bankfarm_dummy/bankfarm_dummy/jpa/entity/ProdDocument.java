package com.bankfarm_dummy.bankfarm_dummy.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prod_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ProdDocumentId.class) // 복합키 클래스 지정
public class ProdDocument {

    @Id
    @Column(name = "bran_id", nullable = false)
    private Long branId; // 지점 ID

    @Id
    @Column(name = "doc_prod_tp", nullable = false, length = 5)
    private String docProdTp; // 상품 타입

    @Id
    @Column(name = "doc_prod_id", nullable = false)
    private Long docProdId; // 상품 ID

    @Id
    @Column(name = "doc_nm", nullable = false, length = 20)
    private String docNm; // 문서 이름

    @Column(name = "doc_crt_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime docCrtAt; // 작성일
}