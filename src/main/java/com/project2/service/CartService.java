package com.project2.service;

import com.project2.entity.CartItem;
import com.project2.entity.Product;
import com.project2.entity.User;
import com.project2.repository.CartRepository;
import com.project2.repository.ProductRepository;
import com.project2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@SuppressWarnings("null")
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    public List<CartItem> getCart() {
        return cartRepository.findByUser(getCurrentUser());
    }

    public void addToCart(Long productId) {
        User user = getCurrentUser();
        cartRepository.findByUserAndProduct_Id(user, productId).ifPresentOrElse(
            item -> {
                item.setQuantity(item.getQuantity() + 1);
                cartRepository.save(item);
            },
            () -> {
                Product product = productRepository.findById(productId).orElseThrow();
                cartRepository.save(new CartItem(user, product, 1));
            }
        );
    }

    public void removeFromCart(Long id) {
        cartRepository.deleteById(id);
    }

    public void updateQuantity(Long id, int quantity) {
        cartRepository.findById(id).ifPresent(item -> {
            item.setQuantity(quantity);
            cartRepository.save(item);
        });
    }

    public BigDecimal getSubtotal() {
        return getCart().stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart() {
        cartRepository.deleteByUser(getCurrentUser());
    }
}
