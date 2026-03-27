package com.project2.service;

import com.project2.entity.*;
import com.project2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@SuppressWarnings("null")
public class SupportChatService {

    @Autowired
    private SupportSessionRepository sessionRepository;

    @Autowired
    private SupportMessageRepository messageRepository;

    @Autowired
    private SupportAttachmentRepository attachmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/support-chat/";

    @Transactional
    public SupportSession startOrGetSession(String email) {
        return sessionRepository.findByUserEmailAndStatus(email, "OPEN")
                .orElseGet(() -> sessionRepository.save(new SupportSession(email)));
    }

    @Transactional
    public SupportMessage sendMessage(Long sessionId, String senderType, String text) {
        SupportSession session = sessionRepository.findById(sessionId).orElseThrow();
        SupportMessage message = new SupportMessage(session, senderType, text);
        message = messageRepository.save(message);

        if ("USER".equals(senderType)) {
            processBotResponse(text, session);
        }

        return message;
    }

    private void processBotResponse(String userText, SupportSession session) {
        String botResponse = generateIntelligence(userText, session);
        if (botResponse != null) {
            SupportMessage botMsg = new SupportMessage(session, "BOT", botResponse);
            messageRepository.save(botMsg);
        }
    }

    private String generateIntelligence(String text, SupportSession session) {
        String lowerText = text.toLowerCase();

        // 0. Handle Active Intent Flows
        if ("CANCEL_ORDER".equals(session.getCurrentIntent())) {
            return handleCancellationFlow(text, session);
        }

        // 1. Order ID Detection (Numeric strings)
        Pattern orderPattern = Pattern.compile("\\b(\\d{1,6})\\b");
        Matcher matcher = orderPattern.matcher(text);
        
        if (matcher.find()) {
            String orderIdStr = matcher.group(1);
            List<Order> orders = orderRepository.searchOrders(orderIdStr);
            if (!orders.isEmpty()) {
                Order order = orders.get(0);
                String date = order.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                
                if (lowerText.contains("status") || lowerText.contains("track") || lowerText.contains("where")) {
                    return "I found your order! **Order #" + order.getId() + "** placed on " + date + 
                           " is currently **" + order.getOrderStatus() + "**. " +
                           "Estimated delivery: 3-5 business days from shipment.";
                } else if (lowerText.contains("refund")) {
                    if ("DELIVERED".equals(order.getOrderStatus())) {
                        return "I see Order #" + order.getId() + " was delivered. You are eligible for a refund within 30 days of purchase. " +
                               "Shall I initiate the refund request for you?";
                    } else {
                        return "Order #" + order.getId() + " is currently " + order.getOrderStatus() + ". Refunds are typically processed after delivery or if the order is cancelled.";
                    }
                } else if (lowerText.contains("return") || lowerText.contains("damaged") || lowerText.contains("replace")) {
                    return "I've noted your request for Order #" + order.getId() + ". Our return process is simple: " +
                           "1. Pack the item in original packaging.\n" +
                           "2. A courier will collect it within 48 hours.\n" +
                           "3. Refund/Replacement will be processed upon inspection.";
                } else if (lowerText.contains("cancel")) {
                    String status = order.getOrderStatus() != null ? order.getOrderStatus().toUpperCase().replace(" ", "_") : "";
                    if ("ORDER_PLACED".equals(status) || "PROCESSING".equals(status)) {
                        session.setCurrentIntent("CANCEL_ORDER");
                        session.setIntentStep("AWAITING_REASON");
                        session.setContextData(order.getId().toString());
                        sessionRepository.save(session);
                        return "Order #" + order.getId() + " is eligible for cancellation. Please provide a **reason for cancellation** to confirm.";
                    } else {
                        return "I'm sorry, Order #" + order.getId() + " is already " + order.getOrderStatus() + " and cannot be cancelled automatically. Please contact our admin.";
                    }
                }
            }
        }

        // 2. Keyword Intents
        if (lowerText.contains("status") || lowerText.contains("track") || lowerText.contains("where")) {
            return "To check your order status, please provide your **Order ID** numbers.";
        }
        
        if (lowerText.contains("refund")) {
            return "We offer full refunds for products returned within 30 days. Please provide your **Order ID** to check eligibility.";
        }

        if (lowerText.contains("return") || lowerText.contains("replace") || lowerText.contains("damaged")) {
            return "I'm sorry to hear that. Could you please provide your **Order ID** and describe the issue? You can also upload a photo of the damaged product.";
        }

        if (lowerText.contains("cancel")) {
            session.setCurrentIntent("CANCEL_ORDER");
            session.setIntentStep("AWAITING_ORDER_ID");
            sessionRepository.save(session);
            return "Orders can be cancelled if they haven't been shipped yet. Please provide your **Order ID** to proceed.";
        }

        if (lowerText.contains("shipping") || lowerText.contains("delivery") || lowerText.contains("time")) {
            return "Standard shipping takes **3-5 business days** within the Nordic region. International shipping may take 7-10 days.";
        }

        if (lowerText.contains("payment") || lowerText.contains("pay")) {
            return "We accept all major **Credit/Debit Cards, UPI, Net Banking, and Cash on Delivery (COD)**.";
        }

        if (lowerText.contains("admin") || lowerText.contains("contact") || lowerText.contains("human") || lowerText.contains("help")) {
            session.setStatus("ESCALATED");
            sessionRepository.save(session);
            return "I've escalated this to our human support team. An admin will be with you shortly. Feel free to leave details or images here.";
        }

        if (lowerText.contains("hi") || lowerText.contains("hello") || lowerText.contains("hey")) {
             return "Hej! I am your ÉPURE Support Assistant. How can I help you with your order, returns, or general questions today?";
        }

        return "I'm not sure I understand. I can help with **Order Tracking**, **Refunds**, **Returns**, or **Cancellations**. You can also ask to **Contact Admin** for more complex queries.";
    }

    private String handleCancellationFlow(String text, SupportSession session) {
        String currentStep = session.getIntentStep();
        
        if ("AWAITING_ORDER_ID".equals(currentStep)) {
            Pattern orderPattern = Pattern.compile("\\b(\\d{1,6})\\b");
            Matcher matcher = orderPattern.matcher(text);
            if (matcher.find()) {
                String orderIdStr = matcher.group(1);
                Long orderId = Long.parseLong(orderIdStr);
                Optional<Order> orderOpt = orderRepository.findById(orderId);
                
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    String status = order.getOrderStatus() != null ? order.getOrderStatus().toUpperCase().replace(" ", "_") : "";
                    if ("ORDER_PLACED".equals(status) || "PROCESSING".equals(status)) {
                        session.setIntentStep("AWAITING_REASON");
                        session.setContextData(orderIdStr);
                        sessionRepository.save(session);
                        return "I found your order #" + orderIdStr + ". Please provide a **reason for cancellation** to proceed.";
                    } else {
                        session.setCurrentIntent(null);
                        session.setIntentStep(null);
                        sessionRepository.save(session);
                        return "Order #" + orderIdStr + " is already " + order.getOrderStatus() + " and cannot be cancelled automatically. How else can I help you?";
                    }
                } else {
                    return "I couldn't find an order with ID #" + orderIdStr + ". Please provide a valid **Order ID**.";
                }
            } else {
                return "Please provide a valid numeric **Order ID** to proceed with cancellation.";
            }
        }
        
        if ("AWAITING_REASON".equals(currentStep)) {
            String orderIdStr = session.getContextData();
            Long orderId = Long.parseLong(orderIdStr);
            Order order = orderRepository.findById(orderId).orElseThrow();
            
            order.setOrderStatus("CANCELLED");
            order.setCancellationReason(text);
            orderRepository.save(order);
            
            session.setCurrentIntent(null);
            session.setIntentStep(null);
            session.setContextData(null);
            sessionRepository.save(session);
            
            return "Perfect. I've cancelled Order #" + orderIdStr + " as requested. Reason: *" + text + "*. Is there anything else I can help you with?";
        }
        
        return "I'm not sure I understand. I can help with **Order Tracking**, **Refunds**, **Returns**, or **Cancellations**.";
    }

    @Transactional
    public SupportMessage uploadFile(Long sessionId, String senderType, MultipartFile file) throws IOException {
        SupportSession session = sessionRepository.findById(sessionId).orElseThrow();
        
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        String fileUrl = "/uploads/support-chat/" + fileName;
        
        SupportMessage message = new SupportMessage(session, senderType, "[Attached File]");
        message = messageRepository.save(message);

        SupportAttachment attachment = new SupportAttachment(message, fileUrl, file.getContentType());
        attachmentRepository.save(attachment);
        
        message.getAttachments().add(attachment);
        return message;
    }

    public List<SupportMessage> getMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public List<SupportSession> getAllActiveSessions() {
        return sessionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void markAsCompleted(Long sessionId) {
        SupportSession session = sessionRepository.findById(sessionId).orElseThrow();
        session.setStatus("COMPLETED");
        sessionRepository.save(session);
    }

    public Optional<SupportSession> getSession(Long sessionId) {
        return sessionRepository.findById(sessionId);
    }
}
