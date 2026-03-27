package com.project2.controller;

import com.project2.entity.Product;
import com.project2.entity.Moodboard;
import com.project2.service.ProductService;
import com.project2.service.MoodboardService;
import com.project2.service.UserService;
import com.project2.service.OrderService;
import com.project2.repository.UserRepository;
import com.project2.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("null")
public class AdminController {

    @Autowired
    private ProductService productService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private MoodboardService moodboardService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("totalOrders", orderService.getTotalOrdersCount());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("activeUsers", userRepository.count());
        model.addAttribute("popularProducts", productService.getAllProducts().stream().limit(5).toList());
        model.addAttribute("title", "Admin Overview");
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("title", "Manage Products");
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("title", "Add New Product");
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("category") Long categoryId) {
        categoryRepository.findById(categoryId).ifPresent(product::setCategory);
        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/moodboards")
    public String manageMoodboards(Model model) {
        model.addAttribute("moodboards", moodboardService.getAllMoodboards());
        model.addAttribute("title", "Manage Moodboards");
        return "admin/moodboards";
    }

    @GetMapping("/moodboards/add")
    public String addMoodboardForm(Model model) {
        model.addAttribute("moodboard", new Moodboard());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("title", "Add New Moodboard");
        return "admin/moodboard-form";
    }

    @PostMapping("/moodboards/save")
    public String saveMoodboard(@ModelAttribute Moodboard moodboard, @RequestParam(value = "selectedProducts", required = false) List<Long> selectedProductIds) {
        if (selectedProductIds != null) {
            moodboard.getProducts().clear();
            for (Long pid : selectedProductIds) {
                productService.getProductById(pid).ifPresent(p -> moodboard.getProducts().add(p));
            }
        }
        moodboardService.saveMoodboard(moodboard);
        return "redirect:/admin/moodboards";
    }

    @GetMapping("/moodboards/delete/{id}")
    public String deleteMoodboard(@PathVariable Long id) {
        moodboardService.deleteMoodboard(id);
        return "redirect:/admin/moodboards";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("title", "User Management");
        return "admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/support")
    public String support(Model model) {
        model.addAttribute("title", "Admin Support");
        return "admin/support";
    }

}
