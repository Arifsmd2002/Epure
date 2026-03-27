package com.project2.repository;

import com.project2.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.vibes) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE LOWER(p.vibes) LIKE LOWER(CONCAT('%', :vibe, '%'))")
    List<Product> findByVibe(@Param("vibe") String vibe);

    @Query("SELECT p FROM Product p WHERE LOWER(p.category.name) = LOWER(:categoryName) OR " +
           "(LOWER(p.category.name) LIKE 'sofa%' AND LOWER(:categoryName) LIKE 'sofa%') OR " +
           "(LOWER(p.category.name) IN ('lighting', 'lamps', 'lamp') AND LOWER(:categoryName) IN ('lighting', 'lamps', 'lamp'))")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    List<Product> findByName(String name);

    @Modifying
    @Query("DELETE FROM Product p WHERE p.name = :name")
    void deleteByNameModifying(@Param("name") String name);

    void deleteByCategory_NameIgnoreCase(String categoryName);
}
