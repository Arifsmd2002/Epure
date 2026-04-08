package com.project2.controller;

import com.project2.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PostMapping("/toggle/{productId}")
    @ResponseBody
    public String toggleWishlist(@PathVariable Long productId) {
        wishlistService.toggleWishlist(productId);
        return "success";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromWishlist(@PathVariable Long productId) {
        wishlistService.toggleWishlist(productId);
        return "redirect:/wishlist";
    }
}
