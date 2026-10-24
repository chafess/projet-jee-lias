package com.fsbm.lias.repository;

import com.fsbm.lias.model.DiscussionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, Long> {
    List<DiscussionMessage> findByDiscussionIdOrderByDateEnvoiAsc(Long discussionId);
}