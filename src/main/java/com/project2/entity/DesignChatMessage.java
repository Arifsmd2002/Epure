package com.project2.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "design_chat_messages")
public class DesignChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private DesignChatSession session;

    // USER or ADMIN
    @JsonProperty("senderType")
    private String senderType;
    
    @Column(columnDefinition = "TEXT")
    @JsonProperty("messageText")
    private String messageText;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DesignChatAttachment> attachments = new ArrayList<>();

    public DesignChatMessage() {}

    public DesignChatMessage(DesignChatSession session, String senderType, String messageText) {
        this.session = session;
        this.senderType = senderType;
        this.messageText = messageText;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DesignChatSession getSession() { return session; }
    public void setSession(DesignChatSession session) { this.session = session; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<DesignChatAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<DesignChatAttachment> attachments) { this.attachments = attachments; }
}
