package com.cartnova.model;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class OrderItem {
    public Long productId;
    public String productName;
    public BigDecimal price;
    public int quantity;
}
