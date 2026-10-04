package com.ritangkar.catalogauditor.service;

import com.ritangkar.catalogauditor.model.CatalogProduct;
import com.ritangkar.catalogauditor.model.CatalogQualityReport;
import com.ritangkar.catalogauditor.model.FindingSummary;
import com.ritangkar.catalogauditor.repository.CatalogRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs all seven checks and combines them into one score. Deduction is
 * per-occurrence, not per-unique-product — a product flagged by two
 * different checks costs the score twice, since it genuinely has two
 * separate, independently-real problems.
 */
@Service
public class CatalogAuditService {

    private static final double ERROR_PENALTY_PER_OCCURRENCE = 1.2;
    private static final double WARNING_PENALTY_PER_OCCURRENCE = 0.6;

    private final CatalogRepository repository;
    private final CatalogQualityChecks checks;

    public CatalogAuditService(CatalogRepository repository, CatalogQualityChecks checks) {
        this.repository = repository;
        this.checks = checks;
    }

    public CatalogQualityReport audit() {
        List<CatalogProduct> products = repository.findAll();

        List<FindingSummary> findings = List.of(
                checks.duplicateCandidates(products),
                checks.missingMaterial(products),
                checks.missingOrShortDescription(products),
                checks.poorTitles(products),
                checks.categoryMismatches(products),
                checks.suspiciousClaims(products),
                checks.inconsistentColors(products)
        );

        double total = 100;
        List<String> breakdown = new ArrayList<>();
        breakdown.add("Starting score: 100 (catalog of " + products.size() + " products)");

        for (FindingSummary f : findings) {
            if (f.count() == 0) continue;
            double perOccurrence = f.severity().equals("ERROR") ? ERROR_PENALTY_PER_OCCURRENCE : WARNING_PENALTY_PER_OCCURRENCE;
            double penalty = f.count() * perOccurrence;
            total -= penalty;
            breakdown.add(String.format("-%.1f (%s: %d occurrence(s) \u00d7 %.1f)",
                    penalty, f.type(), f.count(), perOccurrence));
        }

        int finalScore = (int) Math.round(Math.max(0, Math.min(100, total)));
        breakdown.add("Final score: " + finalScore);

        return new CatalogQualityReport(products.size(), finalScore, findings, breakdown);
    }
}
