package com.inventory_server.controller;

import com.inventory_server.model.Product;
import com.inventory_server.model.StockHistory;
import com.inventory_server.repository.ProductRepository;
import com.inventory_server.repository.StockHistoryRepository;
import com.inventory_server.repository.SupplierRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {
    private final ProductRepository repo;
    private final StockHistoryRepository historyRepo;
    private final SupplierRepository supplierRepo;

    public ProductController(ProductRepository repo, StockHistoryRepository historyRepo, SupplierRepository supplierRepo) {
        this.repo = repo;
        this.historyRepo = historyRepo;
        this.supplierRepo = supplierRepo;
    }

    @GetMapping
    public List<Product> getAll() {
        return repo.findAll();
    }

    /**
     * EXCEL EXPORT: Generates Purchase Order for Stationery items < 15 units
     */
    @GetMapping("/purchase-orders/export")
    public void exportPOToExcel(HttpServletResponse response) throws IOException {
        List<Product> lowStockItems = repo.findAll().stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() < 15)
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Stationery Purchase Order");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Item Name", "Category", "Current Stock", "Supplier", "Suggested Order Qty"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Product p : lowStockItems) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getName());
                row.createCell(1).setCellValue(p.getCategory());
                row.createCell(2).setCellValue(p.getQuantity());

                // FIX: Use the helper method from Product model instead of field access
                row.createCell(3).setCellValue(p.getSupplierName());

                int suggestOrder = 100 - p.getQuantity();
                row.createCell(4).setCellValue(suggestOrder > 0 ? suggestOrder : 0);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=Purchase_Order_Stationery.xlsx");
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * SAVE PRODUCT: Handles the product-supplier relationship automatically via JSON
     */
    @PostMapping
    public Product save(@RequestBody Product product) {
        // We no longer call setSupplierName here because 'supplierName'
        // is now derived from the 'supplier' object relationship.

        String action = (product.getId() == null) ? "ADDED" : "UPDATED";

        // repo.save will automatically handle the foreign key if a supplier is attached to the product object
        Product savedProduct = repo.save(product);

        logStockActivity(savedProduct.getName(), action, savedProduct.getQuantity());
        return savedProduct;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repo.findById(id).map(product -> {
            logStockActivity(product.getName(), "DELETED", 0);
            repo.deleteById(id);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public List<StockHistory> getStockHistory() {
        return historyRepo.findAllByOrderByIdDesc();
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        if (historyRepo.existsById(id)) {
            historyRepo.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/summary")
    public Map<String, Object> getInventorySummary() {
        List<Product> products = repo.findAll();
        long supplierCount = supplierRepo.count();

        double totalValue = products.stream()
                .mapToDouble(p -> (p.getPrice() != null ? p.getPrice() : 0.0) * (p.getQuantity() != null ? p.getQuantity() : 0))
                .sum();

        long lowStockCount = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() < 15)
                .count();

        Map<String, Long> categoryData = products.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory() == null ? "Other" : p.getCategory(),
                        Collectors.counting()
                ));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", products.size());
        summary.put("totalValue", totalValue);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalSuppliers", supplierCount);
        summary.put("categoryData", categoryData);

        return summary;
    }

    private void logStockActivity(String name, String action, Integer qty) {
        StockHistory history = new StockHistory();
        history.setProductName(name);
        history.setAction(action);
        history.setQuantity(qty);
        history.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        historyRepo.save(history);
    }
}