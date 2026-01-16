package com.inventory.client.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SaleModel {
    private Long id;
    private List<String> items;
    private Double totalAmount;
    private String saleDate; // Received as String from JSON for easier parsing

    public SaleModel() {}

    // --- Getters ---
    public Long getId() { return id; }
    public List<String> getItems() { return items; }
    public Double getTotalAmount() { return totalAmount; }

    public LocalDateTime getSaleDate() {
        if (saleDate == null) return LocalDateTime.now();
        try {
            // Handles standard ISO format from Spring Boot
            return LocalDateTime.parse(saleDate);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setItems(List<String> items) { this.items = items; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public void setSaleDate(String saleDate) { this.saleDate = saleDate; }


    public String getFormattedTime() {
        return getSaleDate().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}