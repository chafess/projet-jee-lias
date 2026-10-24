package com.fsbm.lias.repository;

import com.fsbm.lias.model.Convention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConventionRepository extends JpaRepository<Convention, Long> {
}