package com.project2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "support_messages")
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private SupportSession session;

    private String senderType; // USER, BOT, ADMIN
    
    @Column(columnDefinition = "TEXT")
    private String messageText;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportAttachment> attachments = new ArrayList<>();

    public SupportMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public SupportMessage(SupportSession session, String senderType, String text) {
        this();
        this.session = session;
        this.senderType = senderType;
        this.messageText = text;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SupportSession getSession() { return session; }
    public void setSession(SupportSession session) { this.session = session; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<SupportAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<SupportAttachment> attachments) { this.attachments = attachments; }
}
