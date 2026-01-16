package com.inventory_server.repository;

import com.inventory_server.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    // You can add custom queries here later,
    // such as finding sales between specific dates for reports.
}