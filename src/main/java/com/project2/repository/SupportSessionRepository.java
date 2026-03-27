package com.project2.repository;

import com.project2.entity.SupportSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface SupportSessionRepository extends JpaRepository<SupportSession, Long> {
    Optional<SupportSession> findByUserEmailAndStatus(String email, String status);
    List<SupportSession> findAllByOrderByCreatedAtDesc();
}
