package com.project2.controller;

import com.project2.entity.SupportMessage;
import com.project2.entity.SupportSession;
import com.project2.service.SupportChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/admin/support-chat")
public class AdminSupportChatController {

    @Autowired
    private SupportChatService supportService;

    @GetMapping
    public String viewAllSessions(Model model) {
        model.addAttribute("sessions", supportService.getAllActiveSessions());
        model.addAttribute("title", "Customer Support Chats");
        return "admin/support_chats";
    }

    @GetMapping("/{sessionId}")
    public String viewSession(@PathVariable Long sessionId, Model model) {
        SupportSession session = supportService.getSession(sessionId).orElseThrow();
        model.addAttribute("chatSession", session);
        model.addAttribute("messages", supportService.getMessages(sessionId));
        model.addAttribute("title", "Support for " + session.getUserEmail());
        return "admin/support_chat_detail";
    }

    @PostMapping("/reply")
    @ResponseBody
    public ResponseEntity<?> sendReply(@RequestParam Long sessionId, @RequestParam String text) {
        SupportMessage message = supportService.sendMessage(sessionId, "ADMIN", text);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/complete/{sessionId}")
    public String markAsComplete(@PathVariable Long sessionId) {
        supportService.markAsCompleted(sessionId);
        return "redirect:/admin/support-chat";
    }
}
