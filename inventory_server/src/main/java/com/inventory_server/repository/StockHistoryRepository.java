package com.inventory_server.repository;

import com.inventory_server.model.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    List<StockHistory> findAllByOrderByIdDesc();
}