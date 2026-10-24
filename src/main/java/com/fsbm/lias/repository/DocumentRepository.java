package com.fsbm.lias.repository;

import com.fsbm.lias.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Recherche par titre ou type
    List<Document> findByTitleContainingIgnoreCaseOrTypeContainingIgnoreCase(String t, String type);
}