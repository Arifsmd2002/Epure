package com.project2.controller;

import com.project2.entity.DesignChatMessage;
import com.project2.entity.DesignChatSession;
import com.project2.service.DesignChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Controller
@RequestMapping("/admin/design-chat")
public class AdminDesignChatController {

    @Autowired
    private DesignChatService chatService;

    @GetMapping
    public String viewAllSessions(Model model) {
        model.addAttribute("sessions", chatService.getAllActiveSessions());
        model.addAttribute("totalRequests", chatService.getTotalRequestsCount());
        model.addAttribute("activeChats", chatService.getActiveChatsCount());
        model.addAttribute("closedConsultations", chatService.getClosedConsultationsCount());
        model.addAttribute("title", "Design Consultations");
        return "admin/design_chats";
    }

    @GetMapping("/{sessionId}")
    public String viewSession(@PathVariable Long sessionId, Model model) {
        DesignChatSession session = chatService.getSession(sessionId).orElseThrow();
        model.addAttribute("chatSession", session);
        model.addAttribute("messages", chatService.getMessages(sessionId));
        model.addAttribute("title", "Consultation with " + session.getUserEmail());
        return "admin/design_chat_detail";
    }

    @PostMapping("/reply")
    @ResponseBody
    public ResponseEntity<?> sendReply(@RequestParam Long sessionId, @RequestParam String text) {
        DesignChatMessage message = chatService.sendMessage(sessionId, "ADMIN", text);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam Long sessionId, @RequestParam MultipartFile file) {
        try {
            DesignChatMessage message = chatService.uploadFile(sessionId, "ADMIN", file);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/complete/{sessionId}")
    public String markAsComplete(@PathVariable Long sessionId) {
        chatService.markAsCompleted(sessionId);
        return "redirect:/admin/design-chat";
    }
}
