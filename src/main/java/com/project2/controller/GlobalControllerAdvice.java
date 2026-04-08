package com.project2.controller;

import com.project2.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private WishlistService wishlistService;

    @ModelAttribute
    public void addWishlistToModel(Model model) {
        Set<Long> wishlistProductIds = wishlistService.getWishlistProducts().stream()
                .map(p -> p.getId())
                .collect(Collectors.toSet());
        model.addAttribute("wishlistIds", wishlistProductIds);
    }
}
