package com.project2.service;

import com.project2.entity.DesignChatAttachment;
import com.project2.entity.DesignChatMessage;
import com.project2.entity.DesignChatSession;
import com.project2.repository.DesignChatAttachmentRepository;
import com.project2.repository.DesignChatMessageRepository;
import com.project2.repository.DesignChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class DesignChatService {

    @Autowired
    private DesignChatSessionRepository sessionRepository;

    @Autowired
    private DesignChatMessageRepository messageRepository;

    @Autowired
    private DesignChatAttachmentRepository attachmentRepository;

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/design-chat/";

    @Transactional
    public DesignChatSession startOrGetSession(String email) {
        return sessionRepository.findByUserEmailAndStatus(email, "OPEN")
                .orElseGet(() -> sessionRepository.save(new DesignChatSession(email)));
    }

    @Transactional
    public DesignChatMessage sendMessage(Long sessionId, String senderType, String text) {
        DesignChatSession session = sessionRepository.findById(sessionId).orElseThrow();
        DesignChatMessage message = new DesignChatMessage(session, senderType, text);
        return messageRepository.save(message);
    }

    @Transactional
    public DesignChatMessage uploadFile(Long sessionId, String senderType, MultipartFile file) throws IOException {
        DesignChatSession session = sessionRepository.findById(sessionId).orElseThrow();
        
        // Ensure directory exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        String fileUrl = "/uploads/design-chat/" + fileName;
        
        DesignChatMessage message = new DesignChatMessage(session, senderType, "[Attached Image]");
        message = messageRepository.save(message);

        DesignChatAttachment attachment = new DesignChatAttachment(message, fileUrl, file.getContentType());
        attachmentRepository.save(attachment);
        
        message.getAttachments().add(attachment);
        return message;
    }

    public List<DesignChatMessage> getMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public List<DesignChatSession> getAllActiveSessions() {
        return sessionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void markAsCompleted(Long sessionId) {
        DesignChatSession session = sessionRepository.findById(sessionId).orElseThrow();
        session.setStatus("COMPLETED");
        sessionRepository.save(session);
    }

    public Optional<DesignChatSession> getSession(Long sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public long getTotalRequestsCount() {
        return sessionRepository.count();
    }

    public long getActiveChatsCount() {
        return sessionRepository.findAll().stream().filter(s -> "OPEN".equals(s.getStatus())).count();
    }

    public long getClosedConsultationsCount() {
        return sessionRepository.findAll().stream().filter(s -> "COMPLETED".equals(s.getStatus())).count();
    }
}
