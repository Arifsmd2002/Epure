package com.project2.repository;

import com.project2.entity.CartItem;
import com.project2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct_Id(User user, Long productId);
    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
