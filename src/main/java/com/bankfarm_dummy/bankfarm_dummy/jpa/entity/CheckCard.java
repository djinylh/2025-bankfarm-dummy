package com.bankfarm_dummy.bankfarm_dummy.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "check_card")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CheckCard {

    @Id
    private Long cardUserId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @MapsId  // 💡 UserCard의 ID를 그대로 PK로 사용
    @JoinColumn(name = "card_user_id")
    private UserCard userCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_acct_id", nullable = false)
    private Account account;
}