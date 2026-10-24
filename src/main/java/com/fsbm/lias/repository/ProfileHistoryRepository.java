package com.fsbm.lias.repository;

import com.fsbm.lias.model.ProfileHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileHistoryRepository extends JpaRepository<ProfileHistory, Long> {
    List<ProfileHistory> findByMemberIdOrderByDateModificationDesc(Long memberId);
}