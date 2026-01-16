package com.inventory_server.repository;

import com.inventory_server.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Supplier entity.
 * JpaRepository provides standard methods: save(), deleteById(), findAll(), findById().
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    // You can add custom search methods here if needed, for example:
    // List<Supplier> findByNameContainingIgnoreCase(String name);
}