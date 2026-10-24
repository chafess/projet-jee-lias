package com.fsbm.lias.repository;

import com.fsbm.lias.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // CETTE LIGNE MANQUAIT
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Pour la sécurité (doublons)
    Optional<Member> findByEmail(String email);
   //
     List<Member> findByTeamId(Long teamId);

    // Dans MemberRepository.java
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.participatingEvents WHERE m.id = :id")
    Optional<Member> findByIdWithEvents(@Param("id") Long id);
    // Recherche par prénom, nom ou centre d'intérêt (insensible à la casse)
    List<Member> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrCentresInteretContainingIgnoreCase(
            String fn, String ln, String ci);
    // Pour la barre de recherche (Section 18 du PDF)
    List<Member> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}