package com.cartnova.config;

import com.cartnova.model.Product;
import com.cartnova.model.User;
import com.cartnova.repository.ProductRepository;
import com.cartnova.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

/** Inserts demo users and products on the first run (only when the tables are empty). */
@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(UserRepository users, ProductRepository products, PasswordEncoder enc) {
        return args -> {
            if (users.count() == 0) {
                users.save(new User("Admin", "admin@cartnova.com", enc.encode("admin123"), "ADMIN"));
                users.save(new User("Demo Customer", "user@cartnova.com", enc.encode("user123"), "CUSTOMER"));
            }
            if (products.count() == 0) {
                products.saveAll(List.of(
                    p("Smartphone X", "6.5 inch AMOLED, 128 GB", "19999", "Mobiles", 25),
                    p("Wireless Earbuds", "Noise cancelling, 24h battery", "2499", "Electronics", 60),
                    p("Running Shoes", "Lightweight and breathable", "2999", "Fashion", 40),
                    p("Cotton T-Shirt", "100% cotton, regular fit", "599", "Fashion", 100),
                    p("Smart Watch", "Heart-rate and sleep tracking", "3499", "Electronics", 30),
                    p("Backpack", "25L water-resistant", "1299", "Accessories", 50)));
            }
        };
    }

    private Product p(String name, String desc, String price, String cat, int stock) {
        return new Product(name, desc, new BigDecimal(price), cat,
                "https://placehold.co/400x400?text=" + name.replace(' ', '+'), stock);
    }
}
