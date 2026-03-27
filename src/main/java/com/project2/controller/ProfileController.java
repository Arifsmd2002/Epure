package com.project2.controller;

import com.project2.entity.User;
import com.project2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/account/edit")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String showEditProfilePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("title", "Edit Profile");
        
        return "edit-profile";
    }

    @PostMapping
    public String updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElseThrow();
        
        userService.updateUserProfile(user.getId(), firstName, lastName, email);
        
        return "redirect:/account?success=profileUpdated";
    }
}
