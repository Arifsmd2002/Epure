package com.project2.controller;

import com.project2.entity.OtpVerification;
import com.project2.service.OtpService;
import com.project2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> initiateRegistration(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword) {

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match."));
        }

        // Check for duplicate username
        if (userService.usernameExists(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username '" + username + "' is already taken."));
        }

        // Check for duplicate email
        if (userService.emailExists(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "An account with this email already exists."));
        }

        // Initiate OTP verification
        otpService.generateAndSendOtp(firstName, lastName, username, email, password);

        return ResponseEntity.ok(Map.of("message", "OTP sent to your email."));
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        String result = otpService.verifyOtp(email, otp);

        if ("SUCCESS".equals(result)) {
            Optional<OtpVerification> verificationOpt = otpService.getVerification(email);
            if (verificationOpt.isPresent()) {
                OtpVerification v = verificationOpt.get();
                // Finally Create User
                userService.registerNewUser(v.getUsername(), v.getEmail(), v.getPassword(), v.getFirstName(), v.getLastName());
                
                // Cleanup OTP data
                otpService.deleteVerification(email);
                
                return ResponseEntity.ok(Map.of("message", "Registration successful. You can now login."));
            }
        }

        return ResponseEntity.badRequest().body(Map.of("error", result));
    }

    @PostMapping("/resend-otp")
    @ResponseBody
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        String result = otpService.resendOtp(email);
        
        if ("SUCCESS".equals(result)) {
            return ResponseEntity.ok(Map.of("message", "New OTP sent."));
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", result));
    }
}
