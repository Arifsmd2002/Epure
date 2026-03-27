package com.project2.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${admin.email}")
    private String adminEmail;

    public void sendNewOrderEmail(String toEmail, Long orderId, String customerEmail, String totalAmount, String paymentMethod, String orderStatus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(adminEmail);
            helper.setTo(toEmail);
            helper.setSubject("New Order Received (ID: " + orderId + ")");
            
            String htmlContent = generateOrderHtml(orderId, customerEmail, totalAmount, paymentMethod, orderStatus);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send order email: " + e.getMessage());
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        // Log to console so user can test without working SMTP
        System.out.println("----------------------------------------");
        System.out.println("VERIFICATION CODE FOR: " + toEmail);
        System.out.println("OTP IS: " + otp);
        System.out.println("----------------------------------------");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(adminEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your EPURE account");
            
            String htmlContent = generateOtpHtml(otp);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("EMAIL ERROR: Authentication failed. Please check your application.properties.");
        }
    }

    private String generateOtpHtml(String otp) {
        return "<html>" +
               "<head>" +
               "<link href=\"https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700&family=Inter:wght@400;600&display=swap\" rel=\"stylesheet\">" +
               "</head>" +
               "<body style=\"margin: 0; padding: 0; background-color: #ffffff; font-family: 'Inter', sans-serif; color: #1a1a1a; -webkit-font-smoothing: antialiased;\">" +
               "    <div style=\"max-width: 600px; margin: 40px auto; padding: 20px;\">" +
               "        <h1 style=\"font-family: 'Playfair Display', serif; font-size: 2.2rem; margin-bottom: 30px; color: #1a1a1a; text-align: left;\">ÉPURE</h1>" +
               "        " +
               "        <p style=\"font-size: 1.1rem; line-height: 1.6; margin-bottom: 25px; color: #4a4a4a;\">" +
               "            Welcome to ÉPURE — where Scandinavian calm meets thoughtful design." +
               "        </p>" +
               "        " +
               "        <p style=\"font-size: 1rem; line-height: 1.6; margin-bottom: 40px; color: #666;\">" +
               "            Please use the code below to verify your email address and activate your account." +
               "        </p>" +
               "        " +
               "        <div style=\"background-color: #f5f1ea; border-radius: 24px; padding: 60px 40px; text-align: center; margin-bottom: 40px;\">" +
               "            <p style=\"font-size: 0.8rem; letter-spacing: 2.5px; color: #8e7d6a; text-transform: uppercase; margin-bottom: 30px; font-weight: 600;\">" +
               "                YOUR VERIFICATION CODE" +
               "            </p>" +
               "            " +
               "            <h1 style=\"font-size: 4.5rem; margin: 0; color: #1a1a1a; letter-spacing: 15px; font-weight: 700; font-family: 'Inter', sans-serif;\">" +
               "                " + otp + "" +
               "            </h1>" +
               "            " +
               "            <div style=\"margin-top: 35px; display: inline-flex; align-items: center; justify-content: center;\">" +
               "                <span style=\"font-size: 1.1rem; margin-right: 8px;\">⏱</span>" +
               "                <p style=\"font-size: 0.95rem; color: #a58e74; margin: 0; font-weight: 500;\">This code expires in 5 minutes</p>" +
               "            </div>" +
               "        </div>" +
               "        " +
               "        <p style=\"font-size: 0.95rem; line-height: 1.6; color: #888; margin-bottom: 50px;\">" +
               "            If you did not create an ÉPURE account, you can safely ignore this email." +
               "        </p>" +
               "        " +
               "        <div style=\"background-color: #f5f1ea; border-radius: 16px; padding: 35px 20px; text-align: center;\">" +
               "            <p style=\"margin: 0; font-size: 0.9rem; color: #666; font-weight: 500;\">" +
               "                © 2026 ÉPURE. All rights reserved." +
               "            </p>" +
               "            <p style=\"margin: 10px 0 0 0; font-size: 0.9rem; color: #666;\">" +
               "                For support: <a href=\"mailto:hello@epure.com\" style=\"color: #1a1a1a; font-weight: 600; text-decoration: underline;\">hello@epure.com</a>" +
               "            </p>" +
               "        </div>" +
               "    </div>" +
               "</body>" +
               "</html>";
    }

    private String generateOrderHtml(Long orderId, String customerEmail, String totalAmount, String paymentMethod, String orderStatus) {
        return "<html>" +
               "<head>" +
               "<link href=\"https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700&family=Inter:wght@400;600&display=swap\" rel=\"stylesheet\">" +
               "</head>" +
               "<body style=\"margin: 0; padding: 0; background-color: #ffffff; font-family: 'Inter', sans-serif; color: #1a1a1a;\">" +
               "    <div style=\"max-width: 600px; margin: 40px auto; padding: 20px;\">" +
               "        <h1 style=\"font-family: 'Playfair Display', serif; font-size: 2.2rem; margin-bottom: 30px; color: #1a1a1a;\">ÉPURE</h1>" +
               "        <h2 style=\"font-size: 1.5rem; color: #1a1a1a; margin-bottom: 25px; border-bottom: 1px solid #f0f0f0; padding-bottom: 15px;\">New Order Received</h2>" +
               "        <div style=\"background-color: #f9f9f9; border-radius: 16px; padding: 30px; margin-bottom: 30px;\">" +
               "            <table style=\"width: 100%; border-collapse: collapse;\">" +
               "                <tr><td style=\"padding: 10px 0; font-weight: 600; color: #666;\">Order ID</td><td style=\"padding: 10px 0; text-align: right; font-weight: 700;\">#" + orderId + "</td></tr>" +
               "                <tr><td style=\"padding: 10px 0; font-weight: 600; color: #666;\">Customer</td><td style=\"padding: 10px 0; text-align: right;\">" + customerEmail + "</td></tr>" +
               "                <tr><td style=\"padding: 10px 0; font-weight: 600; color: #666;\">Total Amount</td><td style=\"padding: 10px 0; text-align: right; font-weight: 700; color: #1a1a1a;\">₹" + totalAmount + "</td></tr>" +
               "                <tr><td style=\"padding: 10px 0; font-weight: 600; color: #666;\">Payment Method</td><td style=\"padding: 10px 0; text-align: right;\">" + paymentMethod + "</td></tr>" +
               "                <tr><td style=\"padding: 10px 0; font-weight: 600; color: #666;\">Status</td><td style=\"padding: 10px 0; text-align: right;\"><span style=\"background-color: #f5f1ea; color: #8e7d6a; padding: 6px 14px; border-radius: 20px; font-size: 0.85rem; font-weight: 600;\">" + orderStatus + "</span></td></tr>" +
               "            </table>" +
               "        </div>" +
               "        <div style=\"background-color: #f5f1ea; border-radius: 16px; padding: 35px 20px; text-align: center;\">" +
               "            <p style=\"margin: 0; font-size: 0.9rem; color: #666; font-weight: 500;\">© 2026 ÉPURE. All rights reserved.</p>" +
               "        </div>" +
               "    </div>" +
               "</body>" +
               "</html>";
    }
}
