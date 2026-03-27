package com.project2.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "design_chat_sessions")
public class DesignChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Status can be: OPEN, COMPLETED
    private String status = "OPEN";

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DesignChatMessage> messages = new ArrayList<>();

    public DesignChatSession() {}

    public DesignChatSession(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<DesignChatMessage> getMessages() { return messages; }
    public void setMessages(List<DesignChatMessage> messages) { this.messages = messages; }
}
