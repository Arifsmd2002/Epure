package com.project2.repository;

import com.project2.entity.Order;
import com.project2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT o FROM Order o WHERE CAST(o.id AS string) LIKE %:search% OR LOWER(o.email) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY o.createdAt DESC")
    List<Order> searchOrders(@Param("search") String search);
}
