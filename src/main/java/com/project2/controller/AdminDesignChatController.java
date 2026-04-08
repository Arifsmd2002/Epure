package com.project2.controller;

import com.project2.entity.DesignChatMessage;
import com.project2.entity.DesignChatSession;
import com.project2.service.DesignChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    /** Polling endpoint — returns all messages as JSON for real-time sync */
    @GetMapping("/{sessionId}/messages")
    @ResponseBody
    public ResponseEntity<List<DesignChatMessage>> pollMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getMessages(sessionId));
    }

    /** Unified admin reply: supports text + optional image in one request */
    @PostMapping("/reply")
    @ResponseBody
    public ResponseEntity<?> sendReply(
            @RequestParam Long sessionId,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            if ((text == null || text.isBlank()) && (image == null || image.isEmpty())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Message or image required"));
            }
            if (image != null && !image.isEmpty()) {
                String ct = image.getContentType();
                if (ct == null || !ct.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Only image files are allowed"));
                }
                if (image.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Image must be under 5MB"));
                }
            }
            DesignChatMessage message = chatService.sendCombinedMessage(sessionId, "ADMIN", text, image);
            return ResponseEntity.ok(Map.of("success", true, "message", message));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/complete/{sessionId}")
    public String markAsComplete(@PathVariable Long sessionId) {
        chatService.markAsCompleted(sessionId);
        return "redirect:/admin/design-chat";
    }
}
