package com.project2.controller;

import com.project2.entity.Order;
import com.project2.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listOrders(@RequestParam(required = false) String search, Model model) {
        List<Order> orders;
        if (search != null && !search.trim().isEmpty()) {
            orders = orderService.searchOrders(search.trim());
        } else {
            orders = orderService.getAllOrders();
            // Assuming getAllOrders() returns sorted or we just use it directly.
        }
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @PostMapping("/update-status")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders";
    }
}
