package com.cartnova.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @Column(length = 1000)
    public String description;
    public BigDecimal price;
    public String category;
    public String imageUrl;
    public int stock;

    public Product() {}
    public Product(String name, String description, BigDecimal price, String category, String imageUrl, int stock) {
        this.name = name; this.description = description; this.price = price;
        this.category = category; this.imageUrl = imageUrl; this.stock = stock;
    }
}
