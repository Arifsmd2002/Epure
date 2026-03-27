package com.project2.controller;

import com.project2.entity.Order;
import com.project2.entity.User;
import com.project2.service.CartService;
import com.project2.service.OrderService;
import com.project2.service.UserService;
import com.project2.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;

@Controller
@SuppressWarnings("null")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).orElseThrow();
        
        model.addAttribute("user", user);
        model.addAttribute("cart", cartService.getCart());
        model.addAttribute("subtotal", cartService.getSubtotal());
        model.addAttribute("tax", cartService.getSubtotal().multiply(new BigDecimal("0.10")));
        model.addAttribute("total", cartService.getSubtotal().add(cartService.getSubtotal().multiply(new BigDecimal("0.10"))));
        model.addAttribute("title", "Checkout");
        return "checkout";
    }

    @PostMapping("/checkout/process")
    public String processOrder(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String streetAddress,
                               @RequestParam String city,
                               @RequestParam String postalCode,
                               @RequestParam String country,
                               @RequestParam String email,
                               @RequestParam String paymentMethod,
                               @RequestParam(required = false) String upiId,
                               @RequestParam(required = false) String upiName,
                               Model model) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).orElseThrow();

        try {
            if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                streetAddress == null || streetAddress.trim().isEmpty() ||
                city == null || city.trim().isEmpty() ||
                postalCode == null || postalCode.trim().isEmpty() ||
                country == null || country.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                paymentMethod == null || paymentMethod.trim().isEmpty()) {
                
                return "redirect:/checkout?error=validation_failed";
            }

            Order order = orderService.createOrder(user, firstName, lastName, streetAddress, city, postalCode, country, email, paymentMethod);
            
            if ("UPI".equals(paymentMethod)) {
                if (upiId == null || upiId.trim().isEmpty() || !upiId.contains("@") || upiName == null || upiName.trim().isEmpty()) {
                    return "redirect:/checkout?error=invalid_upi_details";
                }
            }

            // Simulate Payment Success
            orderService.updatePaymentStatus(order.getId(), "SUCCESS");
            // Set order status to ORDER_PLACED
            orderService.updateOrderStatus(order.getId(), "ORDER_PLACED");
            
            // Send email notification
            emailService.sendNewOrderEmail("arifsmd7989@gmail.com", order.getId(), order.getEmail(), order.getTotalAmount().toString(), order.getPaymentMethod(), "ORDER_PLACED");

            // Clear cart
            cartService.clearCart();
            
            return "redirect:/order-success?orderId=" + order.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart?error=checkout_failed";
        }
    }

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId).orElseThrow();
        model.addAttribute("order", order);
        model.addAttribute("title", "Order Successful");
        return "order-success";
    }

    @GetMapping("/track-order")
    public String trackOrderPage(Model model) {
        model.addAttribute("title", "Track Your Order");
        return "track-order";
    }

    @GetMapping("/order-history")
    public String orderHistory(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).orElseThrow();
        
        java.util.List<Order> orders = orderService.getUserOrders(user);
        model.addAttribute("orders", orders);
        model.addAttribute("title", "Order History");
        return "order-history";
    }

    @GetMapping("/api/orders/track/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> trackOrderApi(@PathVariable Long orderId, @RequestParam(required = false) String email) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isPresent() && orderOpt.get().getEmail() != null && email != null && orderOpt.get().getEmail().trim().equalsIgnoreCase(email.trim())) {
            Map<String, String> response = new HashMap<>();
            response.put("orderStatus", orderOpt.get().getOrderStatus());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
