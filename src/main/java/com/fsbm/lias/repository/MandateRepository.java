package com.fsbm.lias.repository;

import com.fsbm.lias.model.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.fsbm.lias.model.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
@Repository
public interface MandateRepository extends JpaRepository<Mandate, Long> {
    // Pour trouver le mandat actuel (le Directeur)
    List<Mandate> findAllByTypeAndStatus(String type, String status);

    //List<Mandate> findByTypeAndStatus(String type, String status);
    List<Mandate> findByTypeOrderByStartDateDesc(String type);
    // On utilise List au lieu de Optional pour éviter le crash s'il y a plusieurs chefs d'équipe
    List<Mandate> findByTypeAndStatus(String type, String status);
    List<Mandate> findAllByOrderByStartDateDesc();
}