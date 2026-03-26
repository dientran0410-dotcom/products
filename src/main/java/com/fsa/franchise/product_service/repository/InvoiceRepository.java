package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
}
