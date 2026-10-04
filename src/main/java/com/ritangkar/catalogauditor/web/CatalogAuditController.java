package com.ritangkar.catalogauditor.web;

import com.ritangkar.catalogauditor.model.CatalogProduct;
import com.ritangkar.catalogauditor.model.CatalogQualityReport;
import com.ritangkar.catalogauditor.repository.CatalogRepository;
import com.ritangkar.catalogauditor.service.CatalogAuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CatalogAuditController {

    private final CatalogAuditService auditService;
    private final CatalogRepository repository;

    public CatalogAuditController(CatalogAuditService auditService, CatalogRepository repository) {
        this.auditService = auditService;
        this.repository = repository;
    }

    @GetMapping("/api/catalog/audit")
    public CatalogQualityReport audit() {
        return auditService.audit();
    }

    @GetMapping("/api/catalog/products")
    public List<CatalogProduct> products() {
        return repository.findAll();
    }
}
