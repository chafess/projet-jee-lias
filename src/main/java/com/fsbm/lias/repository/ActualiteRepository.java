package com.fsbm.lias.repository;


import com.fsbm.lias.model.Actualite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActualiteRepository extends JpaRepository<Actualite, Long> {
    // Pour afficher les plus récentes en premier
    List<Actualite> findAllByOrderByDateCreationDesc();
}