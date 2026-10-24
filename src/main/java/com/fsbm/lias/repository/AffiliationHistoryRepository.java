package com.fsbm.lias.repository;

import com.fsbm.lias.model.AffiliationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AffiliationHistoryRepository extends JpaRepository<AffiliationHistory, Long> {
    List<AffiliationHistory> findByMemberIdOrderByDateDebutDesc(Long memberId);
    Optional<AffiliationHistory> findByMemberIdAndDateFinIsNull(Long memberId);
}