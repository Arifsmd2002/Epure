package com.project2.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    private String imageUrl;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "category_id")
    private Category category;

    // Premium/Minimalist metadata
    private String material;
    private String origin;
    private Integer sustainabilityScore; // 1-10
    
    // Lifestyle vibes (search tags e.g. "warm", "minimal", "nordic", "relaxed")
    private String vibes;

    private Double rating; // 1.0 - 5.0

    public Product() {}

    public Product(String name, String description, BigDecimal price, String imageUrl, Category category, String material, String origin, Integer sustainabilityScore, String vibes) {
        this(name, description, price, imageUrl, category, material, origin, sustainabilityScore, vibes, 5.0);
    }

    public Product(String name, String description, BigDecimal price, String imageUrl, Category category, String material, String origin, Integer sustainabilityScore, String vibes, Double rating) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.material = material;
        this.origin = origin;
        this.sustainabilityScore = sustainabilityScore;
        this.vibes = vibes;
        this.rating = rating;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public Integer getSustainabilityScore() { return sustainabilityScore; }
    public void setSustainabilityScore(Integer sustainabilityScore) { this.sustainabilityScore = sustainabilityScore; }
    public String getVibes() { return vibes; }
    public void setVibes(String vibes) { this.vibes = vibes; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
