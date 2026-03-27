package com.project2.controller;

import com.project2.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("")
    public String viewCart(Model model) {
        BigDecimal subtotal = cartService.getSubtotal();
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10")); // 10% tax simulation
        BigDecimal total = subtotal.add(tax);

        model.addAttribute("cart", cartService.getCart());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
        model.addAttribute("title", "My Shopping Cart");
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId) {
        cartService.addToCart(productId);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long id, @RequestParam int quantity) {
        cartService.updateQuantity(id, quantity);
        return "redirect:/cart";
    }
}
