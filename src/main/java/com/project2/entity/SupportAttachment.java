package com.project2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "support_attachments")
public class SupportAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id")
    @JsonIgnore
    private SupportMessage message;

    private String fileUrl;
    private String fileType;
    private LocalDateTime createdAt;

    public SupportAttachment() {
        this.createdAt = LocalDateTime.now();
    }

    public SupportAttachment(SupportMessage message, String fileUrl, String fileType) {
        this();
        this.message = message;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SupportMessage getMessage() { return message; }
    public void setMessage(SupportMessage message) { this.message = message; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
