package com.project2.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "design_chat_attachments")
public class DesignChatAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id")
    @JsonIgnore
    private DesignChatMessage message;

    private String fileUrl;
    private String fileType;

    public DesignChatAttachment() {}

    public DesignChatAttachment(DesignChatMessage message, String fileUrl, String fileType) {
        this.message = message;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DesignChatMessage getMessage() { return message; }
    public void setMessage(DesignChatMessage message) { this.message = message; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
