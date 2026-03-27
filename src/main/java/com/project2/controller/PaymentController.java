package com.project2.controller;

import com.project2.entity.Order;
import com.project2.entity.Payment;
import com.project2.repository.PaymentRepository;
import com.project2.service.CartService;
import com.project2.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam String paymentId,
                                 @RequestParam String orderId,
                                 @RequestParam String signature,
                                 @RequestParam Long dbOrderId) {
        
        // In a real application, you should verify the razorpay signature here
        
        orderService.updatePaymentStatus(dbOrderId, "SUCCESS");
        
        Order order = orderService.getOrderById(dbOrderId).orElseThrow();
        Payment payment = new Payment(order, paymentId, "SUCCESS", order.getPaymentMethod());
        paymentRepository.save(payment);
        
        cartService.clearCart();
        
        return "redirect:/order-success?orderId=" + dbOrderId;
    }
}
