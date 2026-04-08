package com.project2.service;

import com.project2.entity.WishlistItem;
import com.project2.entity.Product;
import com.project2.entity.User;
import com.project2.repository.WishlistRepository;
import com.project2.repository.ProductRepository;
import com.project2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<Product> getWishlistProducts() {
        User user = getCurrentUser();
        if (user == null) return List.of();
        return wishlistRepository.findByUser(user).stream()
                .map(WishlistItem::getProduct)
                .collect(Collectors.toList());
    }

    public void toggleWishlist(Long productId) {
        User user = getCurrentUser();
        if (user == null) return;
        
        wishlistRepository.findByUserAndProduct_Id(user, productId).ifPresentOrElse(
            item -> wishlistRepository.delete(item),
            () -> {
                Product product = productRepository.findById(productId).orElseThrow();
                wishlistRepository.save(new WishlistItem(user, product));
            }
        );
    }

    public boolean isInWishlist(Long productId) {
        User user = getCurrentUser();
        if (user == null) return false;
        return wishlistRepository.findByUserAndProduct_Id(user, productId).isPresent();
    }
}
