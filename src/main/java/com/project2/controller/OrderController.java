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
import com.project2.entity.Product;
import com.project2.service.ProductService;
import jakarta.servlet.http.HttpSession;


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

    @Autowired
    private ProductService productService;

    @PostMapping("/buy-now/{productId}")
    public String buyNow(@PathVariable Long productId, HttpSession session) {
        Product product = productService.getProductById(productId).orElse(null);
        if (product == null) {
            return "redirect:/shop?error=product_not_found";
        }

        session.setAttribute("buyNowProduct", product);
        session.setAttribute("buyNowQuantity", 1);
        return "redirect:/checkout";
    }


    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(username).orElseThrow();
        
        Product buyNowProduct = (Product) session.getAttribute("buyNowProduct");
        
        if (buyNowProduct != null) {
            int quantity = (int) session.getAttribute("buyNowQuantity");
            BigDecimal subtotal = buyNowProduct.getPrice().multiply(new BigDecimal(quantity));
            BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
            BigDecimal total = subtotal.add(tax);

            model.addAttribute("isBuyNow", true);
            model.addAttribute("product", buyNowProduct);
            model.addAttribute("quantity", quantity);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("tax", tax);
            model.addAttribute("total", total);
        } else {
            model.addAttribute("isBuyNow", false);
            model.addAttribute("cart", cartService.getCart());
            model.addAttribute("subtotal", cartService.getSubtotal());
            model.addAttribute("tax", cartService.getSubtotal().multiply(new BigDecimal("0.10")));
            model.addAttribute("total", cartService.getSubtotal().add(cartService.getSubtotal().multiply(new BigDecimal("0.10"))));
        }
        
        model.addAttribute("user", user);
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
                               @RequestParam(required = false) Integer quantity,
                               HttpSession session,
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

            Product buyNowProduct = (Product) session.getAttribute("buyNowProduct");
            Order order;

            if (buyNowProduct != null) {
                int finalQuantity = (quantity != null) ? quantity : (int) session.getAttribute("buyNowQuantity");
                order = orderService.createOrderFromProduct(user, buyNowProduct, finalQuantity, firstName, lastName, streetAddress, city, postalCode, country, email, paymentMethod);
                session.removeAttribute("buyNowProduct");
                session.removeAttribute("buyNowQuantity");
            } else {

                order = orderService.createOrder(user, firstName, lastName, streetAddress, city, postalCode, country, email, paymentMethod);
                // Clear cart
                cartService.clearCart();
            }
            
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
