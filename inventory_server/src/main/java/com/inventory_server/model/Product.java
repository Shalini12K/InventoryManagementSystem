package com.inventory_server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private Integer quantity;
    private Double price;

    // --- CONNECTING TO SUPPLIER ENTITY ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id") // This creates a Foreign Key in your DB
    private Supplier supplier;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    // Updated Getter and Setter to use the Supplier Object
    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    // Helper method for your Purchase Order logic
    public String getSupplierName() {
        return (supplier != null) ? supplier.getName() : "Unassigned";
    }
}