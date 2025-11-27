package com.bankfarm_dummy.bankfarm_dummy.jpa.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProdDocumentId implements Serializable {
    private Long branId;
    private String docProdTp;
    private Long docProdId;
    private String docNm;
}