package com.cartnova.controller;

import com.cartnova.model.Product;
import com.cartnova.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Public product catalog: search, category filter, detail. */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository repo;

    public ProductController(ProductRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String q,
                              @RequestParam(required = false) String category) {
        boolean hasQ = q != null && !q.isBlank(), hasC = category != null && !category.isBlank();
        if (hasQ && hasC) return repo.findByNameContainingIgnoreCaseAndCategoryIgnoreCase(q, category);
        if (hasQ) return repo.findByNameContainingIgnoreCase(q);
        if (hasC) return repo.findByCategoryIgnoreCase(category);
        return repo.findAll();
    }

    @GetMapping("/categories")
    public List<String> categories() { return repo.categories(); }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
