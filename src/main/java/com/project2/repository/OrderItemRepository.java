package com.project2.repository;

import com.project2.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
