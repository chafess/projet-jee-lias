package com.fsbm.lias.repository;

import com.fsbm.lias.model.MaterialRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {

}