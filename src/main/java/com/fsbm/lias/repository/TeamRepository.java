package com.fsbm.lias.repository;

import com.fsbm.lias.model.Team; // Cette ligne est indispensable !
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}