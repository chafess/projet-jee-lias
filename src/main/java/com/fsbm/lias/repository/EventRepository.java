package com.fsbm.lias.repository;

import com.fsbm.lias.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
public interface EventRepository extends JpaRepository<Event, Long> {
    // Recherche par titre ou lieu
    List<Event> findByTitleContainingIgnoreCaseOrLieuContainingIgnoreCase(String t, String l);
}

