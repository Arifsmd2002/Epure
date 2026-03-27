package com.project2.service;

import com.project2.entity.OtpVerification;
import com.project2.repository.OtpVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpVerificationRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void generateAndSendOtp(String firstName, String lastName, String username, String email, String password) {
        // Clear any existing verification for this email
        otpRepository.deleteByEmail(email);

        String otp = String.format("%06d", new Random().nextInt(1000000));
        OtpVerification verification = new OtpVerification(firstName, lastName, username, email, password, otp);
        otpRepository.save(verification);

        emailService.sendOtpEmail(email, otp);
    }

    @Transactional
    public String verifyOtp(String email, String code) {
        Optional<OtpVerification> optVerification = otpRepository.findByEmail(email);

        if (optVerification.isEmpty()) {
            return "No registration found for this email.";
        }

        OtpVerification verification = optVerification.get();

        if (verification.getAttempts() >= 5) {
            return "Too many failed attempts. Please request a new OTP.";
        }

        if (verification.isExpired()) {
            return "OTP expired, please request a new one";
        }

        if (!verification.getOtpCode().equals(code)) {
            verification.incrementAttempts();
            otpRepository.save(verification);
            return "Invalid OTP";
        }

        return "SUCCESS";
    }

    @Transactional
    public String resendOtp(String email) {
        Optional<OtpVerification> optVerification = otpRepository.findByEmail(email);

        if (optVerification.isEmpty()) {
            return "No registration found for this email.";
        }

        OtpVerification verification = optVerification.get();

        if (!verification.canResend()) {
            return "Please wait before requesting another OTP.";
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));
        verification.setOtpCode(otp);
        verification.setExpiry(LocalDateTime.now().plusMinutes(5));
        verification.setResendAfter(LocalDateTime.now().plusSeconds(30));
        verification.setAttempts(0); // Reset attempts on resend
        otpRepository.save(verification);

        emailService.sendOtpEmail(email, otp);
        return "SUCCESS";
    }

    public Optional<OtpVerification> getVerification(String email) {
        return otpRepository.findByEmail(email);
    }

    @Transactional
    public void deleteVerification(String email) {
        otpRepository.deleteByEmail(email);
    }
}
