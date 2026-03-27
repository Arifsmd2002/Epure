package com.project2.repository;

import com.project2.entity.DesignChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface DesignChatSessionRepository extends JpaRepository<DesignChatSession, Long> {
    Optional<DesignChatSession> findByUserEmailAndStatus(String userEmail, String status);
    List<DesignChatSession> findAllByOrderByCreatedAtDesc();
}
