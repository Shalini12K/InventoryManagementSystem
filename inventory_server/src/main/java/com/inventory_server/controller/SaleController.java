package com.inventory_server.controller;

import com.inventory_server.model.Sale;
import com.inventory_server.model.Product;
import com.inventory_server.repository.SaleRepository;
import com.inventory_server.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
public class SaleController {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<?> recordSale(@RequestBody Sale sale) {
        try {
            // 1. Validate Stock and Update Inventory
            updateInventoryStock(sale.getItems());

            // 2. Set current timestamp
            sale.setSaleDate(LocalDateTime.now());

            // 3. Save the sale record
            Sale savedSale = saleRepository.save(sale);

            return ResponseEntity.ok(savedSale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    private void updateInventoryStock(List<String> items) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Cannot process a sale with no items.");
        }

        for (String itemEntry : items) {
            try {
                // Expected format: "Product Name (x5)"
                int lastIndex = itemEntry.lastIndexOf(" (x");
                if (lastIndex == -1) continue;

                String name = itemEntry.substring(0, lastIndex);
                int qtySold = Integer.parseInt(itemEntry.substring(lastIndex + 3, itemEntry.length() - 1));

                Optional<Product> productOpt = productRepository.findByName(name);

                if (productOpt.isPresent()) {
                    Product product = productOpt.get();

                    // FIXED: Validation logic and braces
                    if (product.getQuantity() < qtySold) {
                        throw new RuntimeException("Insufficient stock for: " + name +
                                " (Requested: " + qtySold + ", Available: " + product.getQuantity() + ")");
                    }

                    product.setQuantity(product.getQuantity() - qtySold);
                    productRepository.save(product);
                } else {
                    throw new RuntimeException("Product not found in inventory: " + name);
                }
            } catch (RuntimeException e) {
                // Keep the specific stock error messages
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Error processing item '" + itemEntry + "': " + e.getMessage());
            }
        }
    }
}