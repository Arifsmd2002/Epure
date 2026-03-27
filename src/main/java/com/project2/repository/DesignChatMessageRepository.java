package com.project2.repository;

import com.project2.entity.DesignChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DesignChatMessageRepository extends JpaRepository<DesignChatMessage, Long> {
    List<DesignChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
