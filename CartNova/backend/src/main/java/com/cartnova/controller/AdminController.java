package com.cartnova.controller;

import com.cartnova.model.Order;
import com.cartnova.model.Product;
import com.cartnova.repository.OrderRepository;
import com.cartnova.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** ADMIN-only endpoints (enforced in SecurityConfig: /api/admin/** needs ROLE_ADMIN). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final List<String> STATUSES = List.of("PLACED", "PACKED", "SHIPPED", "DELIVERED", "CANCELLED");

    private final ProductRepository products;
    private final OrderRepository orders;

    public AdminController(ProductRepository products, OrderRepository orders) {
        this.products = products; this.orders = orders;
    }

    @PostMapping("/products")
    public Product create(@RequestBody Product p) { p.id = null; return products.save(p); }

    @PutMapping("/products/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product p) {
        Product old = products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        old.name = p.name; old.description = p.description; old.price = p.price;
        old.category = p.category; old.imageUrl = p.imageUrl; old.stock = p.stock;
        return products.save(old);
    }

    @PatchMapping("/products/{id}/stock")
    public Product stock(@PathVariable Long id, @RequestParam int stock) {
        Product p = products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.stock = Math.max(0, stock);
        return products.save(p);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { products.deleteById(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/orders")
    public List<Order> allOrders() { return orders.findAllByOrderByCreatedAtDesc(); }

    @PatchMapping("/orders/{id}/status")
    public Order status(@PathVariable Long id, @RequestParam String status) {
        if (!STATUSES.contains(status)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        Order o = orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        o.status = status;
        return orders.save(o);
    }
}
