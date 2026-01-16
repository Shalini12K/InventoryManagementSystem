package com.inventory_server.repository;

import com.inventory_server.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Counts products with stock < 10
    long countByQuantityLessThan(int threshold);
    Optional<Product> findByName(String name);


}
