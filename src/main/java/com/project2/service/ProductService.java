package com.project2.service;

import com.project2.entity.Product;
import com.project2.repository.CartRepository;
import com.project2.repository.MoodboardRepository;
import com.project2.repository.OrderItemRepository;
import com.project2.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MoodboardRepository moodboardRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    public List<Product> getProductsByVibe(String vibe) {
        return productRepository.findByVibe(vibe);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            // 1. Remove from all Moodboards efficiently
            moodboardRepository.removeProductFromAllMoodboards(id);

            // 2. Remove from Carts efficiently
            cartRepository.deleteByProductId(id);

            // 3. Remove from OrderItems efficiently
            orderItemRepository.deleteByProductId(id);

            // Now we can safely delete the product
            productRepository.delete(product);
        }
    }

    @Transactional
    public void bulkDeleteByCategory(String categoryName) {
        List<Product> products = productRepository.findByCategoryName(categoryName);
        for (Product product : products) {
            moodboardRepository.removeProductFromAllMoodboards(product.getId());
            cartRepository.deleteByProductId(product.getId());
            orderItemRepository.deleteByProductId(product.getId());
        }
        productRepository.deleteByCategory_NameIgnoreCase(categoryName);
    }
}
