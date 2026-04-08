package com.project2.repository;

import com.project2.entity.WishlistItem;
import com.project2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUser(User user);
    Optional<WishlistItem> findByUserAndProduct_Id(User user, Long productId);
    void deleteByUserAndProduct_Id(User user, Long productId);
}
