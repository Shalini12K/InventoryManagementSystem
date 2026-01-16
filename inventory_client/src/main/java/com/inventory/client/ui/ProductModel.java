package com.inventory.client.ui;

import java.util.Map;

public class ProductModel {
    private Long id;
    private String name;
    private String category;
    private Integer quantity;
    private Double price;
    private Map<String, Object> supplier;

    // --- Constructors ---
    public ProductModel() {} // Required for JSON parsing

    public ProductModel(String name, String category, Integer quantity, Double price) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

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

    public Map<String, Object> getSupplier() { return supplier; }
    public void setSupplier(Map<String, Object> supplier) { this.supplier = supplier; }

    // --- FIX: This makes the name show up in the POS ComboBox ---
    @Override
    public String toString() {
        return (name != null) ? name : "Unnamed Product";
    }
}