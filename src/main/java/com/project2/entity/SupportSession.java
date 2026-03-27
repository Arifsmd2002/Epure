package com.project2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_sessions")
public class SupportSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private String status; // OPEN, COMPLETED, ESCALATED (to admin)
    private String currentIntent; // e.g. CANCEL_ORDER
    private String intentStep; // e.g. AWAITING_ORDER_ID
    private String contextData; // Store values like orderId
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportMessage> messages = new ArrayList<>();

    public SupportSession() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "OPEN";
    }

    public SupportSession(String email) {
        this();
        this.userEmail = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentIntent() { return currentIntent; }
    public void setCurrentIntent(String currentIntent) { this.currentIntent = currentIntent; }
    public String getIntentStep() { return intentStep; }
    public void setIntentStep(String intentStep) { this.intentStep = intentStep; }
    public String getContextData() { return contextData; }
    public void setContextData(String contextData) { this.contextData = contextData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<SupportMessage> getMessages() { return messages; }
    public void setMessages(List<SupportMessage> messages) { this.messages = messages; }
}
