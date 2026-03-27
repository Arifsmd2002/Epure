package com.project2.service;

import com.project2.entity.*;
import com.project2.repository.OrderRepository;
import com.project2.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@SuppressWarnings("null")
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    public Order createOrder(User user, String firstName, String lastName, String streetAddress, 
                             String city, String postalCode, String country, String email, String paymentMethod) {
        
        List<CartItem> cartItems = cartService.getCart();
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setFirstName(firstName);
        order.setLastName(lastName);
        order.setStreetAddress(streetAddress);
        order.setCity(city);
        order.setPostalCode(postalCode);
        order.setCountry(country);
        order.setEmail(email);
        order.setPaymentMethod(paymentMethod);
        
        BigDecimal subtotal = cartService.getSubtotal();
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10")); // 10% tax
        order.setTotalAmount(subtotal.add(tax));

        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(order, cartItem.getProduct(), 
                                                cartItem.getQuantity(), cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);
            order.getItems().add(orderItem);
        }

        return order;
    }

    public void updateOrderStatus(Long orderId, String status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setOrderStatus(status);
            orderRepository.save(order);
        });
    }

    public void updatePaymentStatus(Long orderId, String status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setPaymentStatus(status);
            if ("SUCCESS".equals(status)) {
                order.setOrderStatus("Processing");
            }
            orderRepository.save(order);
        });
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> searchOrders(String search) {
        return orderRepository.searchOrders(search);
    }

    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> "SUCCESS".equals(o.getPaymentStatus()) || "Processing".equals(o.getOrderStatus()) || "Delivered".equals(o.getOrderStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
