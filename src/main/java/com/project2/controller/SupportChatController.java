package com.project2.controller;

import com.project2.entity.SupportMessage;
import com.project2.entity.SupportSession;
import com.project2.service.SupportChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/support-chat")
public class SupportChatController {

    @Autowired
    private SupportChatService supportService;

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    @PostMapping("/start")
    public ResponseEntity<?> startSession(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        
        if (email == null || !Pattern.compile(EMAIL_PATTERN).matcher(email).matches()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Invalid email format"));
        }

        SupportSession session = supportService.startOrGetSession(email);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "conversationId", session.getId().toString()
        ));
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> payload) {
        try {
            String conversationIdStr = payload.get("conversationId");
            String sender = payload.get("sender"); // USER, ADMIN
            String message = payload.get("message");
            
            Long sessionId = Long.parseLong(conversationIdStr);
            SupportMessage supportMessage = supportService.sendMessage(sessionId, sender.toUpperCase(), message);
            
            return ResponseEntity.ok(Map.of("success", true, "message", supportMessage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam Long sessionId, @RequestParam MultipartFile file) {
        try {
            SupportMessage message = supportService.uploadFile(sessionId, "USER", file);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<SupportMessage>> getMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(supportService.getMessages(sessionId));
    }
}
