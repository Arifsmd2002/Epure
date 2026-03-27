package com.project2.controller;

import com.project2.entity.Product;
import com.project2.entity.Moodboard;
import com.project2.service.ProductService;
import com.project2.service.MoodboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class MainController {

    @Autowired
    private ProductService productService;
    @Autowired
    private MoodboardService moodboardService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Premium Nordic Living");
        return "index";
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) String query,
                       @RequestParam(required = false) String vibe,
                       @RequestParam(required = false) String category,
                       Model model) {
        
        List<Product> products;
        if (query != null && !query.isEmpty()) {
            products = productService.searchProducts(query);
            model.addAttribute("searchQuery", query);
        } else if (vibe != null && !vibe.isEmpty()) {
            products = productService.getProductsByVibe(vibe);
            model.addAttribute("activeVibe", vibe);
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
            model.addAttribute("activeCategory", category);
        } else {
            products = productService.getAllProducts();
        }
        
        model.addAttribute("products", products);
        model.addAttribute("title", "Shop the Collection");
        return "shop";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/shop";
        }
        Product product = productOpt.get();
        model.addAttribute("product", product);
        
        // Fetch recommendations (same category, excluding current product)
        List<Product> recommendations = productService.getProductsByCategory(product.getCategory().getName())
                .stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("recommendations", recommendations);
        
        model.addAttribute("title", product.getName());
        return "product-detail";
    }

    @GetMapping("/collections")
    public String collections(Model model) {
        model.addAttribute("title", "Collections");
        return "collections";
    }

    @GetMapping("/moodboards")
    public String moodboards(Model model) {
        model.addAttribute("moodboards", moodboardService.getAllMoodboards());
        model.addAttribute("title", "Moodboards");
        return "moodboards";
    }

    @GetMapping({"/moodboards/{idOrSlug}", "/moodboard/{idOrSlug}"})
    public String moodboardDetail(@PathVariable String idOrSlug, Model model) {
        Optional<Moodboard> moodboardOpt;
        try {
            Long id = Long.parseLong(idOrSlug);
            moodboardOpt = moodboardService.getMoodboardById(id);
        } catch (NumberFormatException e) {
            moodboardOpt = moodboardService.getMoodboardBySlug(idOrSlug);
        }
        
        if (moodboardOpt.isEmpty()) {
            return "redirect:/moodboards";
        }
        
        model.addAttribute("moodboard", moodboardOpt.get());
        model.addAttribute("title", moodboardOpt.get().getName());
        return "moodboard-detail";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "Our Story");
        return "about";
    }

    @GetMapping("/support")
    public String support(Model model) {
        model.addAttribute("title", "Customer Support");
        return "support";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/account")
    public String account(Model model) {
        model.addAttribute("title", "My Account");
        return "account";
    }
}
