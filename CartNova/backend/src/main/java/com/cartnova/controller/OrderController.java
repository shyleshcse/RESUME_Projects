package com.cartnova.controller;

import com.cartnova.model.*;
import com.cartnova.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/** Checkout + order history for the logged-in customer. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    record Line(Long productId, int quantity) {}
    record OrderReq(List<Line> items) {}

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;

    public OrderController(OrderRepository orders, ProductRepository products, UserRepository users) {
        this.orders = orders; this.products = products; this.users = users;
    }

    @PostMapping
    @Transactional   // any exception rolls back the stock changes
    public Order place(@RequestBody OrderReq req, Authentication auth) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        if (req.items() == null || req.items().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");

        Order o = new Order();
        o.userId = u.id; o.userEmail = u.email;
        BigDecimal total = BigDecimal.ZERO;

        for (Line l : req.items()) {
            Product p = products.findById(l.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));
            if (l.quantity() < 1)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
            if (p.stock < l.quantity())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for " + p.name);
            p.stock -= l.quantity();
            products.save(p);

            OrderItem it = new OrderItem();
            it.productId = p.id; it.productName = p.name; it.price = p.price; it.quantity = l.quantity();
            o.items.add(it);
            total = total.add(p.price.multiply(BigDecimal.valueOf(l.quantity())));
        }
        o.total = total;
        return orders.save(o);
    }

    @GetMapping("/my")
    public List<Order> mine(Authentication auth) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        return orders.findByUserIdOrderByCreatedAtDesc(u.id);
    }
}
