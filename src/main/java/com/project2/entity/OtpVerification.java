package com.project2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Temporary user details
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password; // Raw password temporarily stored until verified

    // OTP details
    private String otpCode;
    private LocalDateTime expiry;
    private LocalDateTime resendAfter;
    private int attempts;

    public OtpVerification() {}

    public OtpVerification(String firstName, String lastName, String username, String email, String password, String otpCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.otpCode = otpCode;
        this.expiry = LocalDateTime.now().plusMinutes(5);
        this.resendAfter = LocalDateTime.now().plusSeconds(30);
        this.attempts = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getExpiry() { return expiry; }
    public void setExpiry(LocalDateTime expiry) { this.expiry = expiry; }

    public LocalDateTime getResendAfter() { return resendAfter; }
    public void setResendAfter(LocalDateTime resendAfter) { this.resendAfter = resendAfter; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    
    public void incrementAttempts() { this.attempts++; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiry);
    }
    
    public boolean canResend() {
        return LocalDateTime.now().isAfter(resendAfter);
    }
}
