package com.project2.repository;

import com.project2.entity.Moodboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MoodboardRepository extends JpaRepository<Moodboard, Long> {
    List<Moodboard> findByNameContainingIgnoreCase(String name);
    Optional<Moodboard> findBySlug(String slug);
    Optional<Moodboard> findFirstBySlug(String slug);

    @Modifying
    @Query(value = "DELETE FROM moodboard_products WHERE product_id = :productId", nativeQuery = true)
    void removeProductFromAllMoodboards(@Param("productId") Long productId);

    @Modifying
    @Query(value = "DELETE FROM moodboards WHERE slug = :slug AND id NOT IN (SELECT MIN(id) FROM moodboards WHERE slug = :slug)", nativeQuery = true)
    void deleteDuplicatesBySlug(@Param("slug") String slug);
}
