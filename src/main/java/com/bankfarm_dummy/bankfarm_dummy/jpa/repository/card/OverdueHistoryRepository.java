package com.bankfarm_dummy.bankfarm_dummy.jpa.repository.card;

import com.bankfarm_dummy.bankfarm_dummy.jpa.entity.OverdueHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverdueHistoryRepository extends JpaRepository<OverdueHistory, String> {
}
