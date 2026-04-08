package com.project2.controller;

import com.project2.entity.DesignChatMessage;
import com.project2.entity.DesignChatSession;
import com.project2.service.DesignChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/consultation")
public class DesignChatController {

    @Autowired
    private DesignChatService chatService;

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    @PostMapping("/start")
    public ResponseEntity<?> startSession(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        
        if (email == null || !Pattern.compile(EMAIL_PATTERN).matcher(email).matches()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Invalid email format"));
        }

        DesignChatSession session = chatService.startOrGetSession(email);
        
        // Return exactly as requested: { "success": true, "conversationId": "abc123" }
        return ResponseEntity.ok(Map.of(
            "success", true,
            "conversationId", session.getId().toString()
        ));
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> payload) {
        try {
            String conversationIdStr = payload.get("conversationId");
            String sender = payload.get("sender");
            String message = payload.get("message");
            
            Long sessionId = Long.parseLong(conversationIdStr);
            DesignChatMessage chatMessage = chatService.sendMessage(sessionId, sender.toUpperCase(), message);
            
            return ResponseEntity.ok(Map.of("success", true, "message", chatMessage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping(value = "/send", consumes = {"multipart/form-data"})
    public ResponseEntity<?> sendUnified(@RequestParam Long sessionId, 
                                         @RequestParam(required = false) String text, 
                                         @RequestParam(required = false, name = "file") MultipartFile file) {
        try {
            DesignChatMessage chatMessage = chatService.sendCombinedMessage(sessionId, "USER", text, file);
            return ResponseEntity.ok(Map.of("success", true, "message", chatMessage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam Long sessionId, @RequestParam MultipartFile file) {
        try {
            DesignChatMessage message = chatService.uploadFile(sessionId, "USER", file);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<DesignChatMessage>> getMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getMessages(sessionId));
    }
}
