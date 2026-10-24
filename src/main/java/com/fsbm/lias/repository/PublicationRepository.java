/*package com.fsbm.lias.repository;

import com.fsbm.lias.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    long countByAuthorId(Long id);

    // Grâce à cette interface, tu pourras enregistrer des publications en une seule ligne !
    // Recherche par titre ou journal
}*/
package com.fsbm.lias.repository;

import com.fsbm.lias.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    long countByAuthorId(Long id);

    // Recherche par titre ou nom de journal
    List<Publication> findByTitleContainingIgnoreCaseOrJournalContainingIgnoreCase(String title, String journal);
}