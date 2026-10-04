package com.ritangkar.catalogauditor.model;

import java.util.List;

public record CatalogQualityReport(
        int totalProducts,
        int overallScore,
        List<FindingSummary> findings,
        List<String> scoreBreakdown
) {
}
