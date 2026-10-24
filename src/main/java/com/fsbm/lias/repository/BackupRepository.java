package com.fsbm.lias.repository;
import com.fsbm.lias.model.Backup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BackupRepository extends JpaRepository<Backup, Long> {
    List<Backup> findAllByOrderByDateCreationDesc();
}