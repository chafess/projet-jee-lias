/*package com.fsbm.lias.repository;
import com.fsbm.lias.model.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
}*/
package com.fsbm.lias.repository;

import com.fsbm.lias.model.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    List<Discussion> findByScope(String scope);
    List<Discussion> findByScopeAndTeamId(String scope, Long teamId);
}