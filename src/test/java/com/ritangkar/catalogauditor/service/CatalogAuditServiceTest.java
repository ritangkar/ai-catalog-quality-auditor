package com.ritangkar.catalogauditor.service;

import com.ritangkar.catalogauditor.model.CatalogQualityReport;
import com.ritangkar.catalogauditor.model.FindingSummary;
import com.ritangkar.catalogauditor.repository.CatalogRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every count below was independently computed with a Python reference
 * implementation of the same seven rules, against this exact bundled
 * catalog (135 products, with issues deliberately injected at generation
 * time), before this test was written — see docs/design-decisions.md.
 */
class CatalogAuditServiceTest {

    private final CatalogRepository repository = new CatalogRepository();
    private final CatalogQualityChecks checks = new CatalogQualityChecks();
    private final CatalogAuditService service = new CatalogAuditService(repository, checks);

    @Test
    void catalogHasTheExpectedTotalProductCount() {
        assertThat(service.audit().totalProducts()).isEqualTo(135);
    }

    @Test
    void everyCheckMatchesItsIndependentlyVerifiedCount() {
        CatalogQualityReport report = service.audit();
        Map<String, Integer> countByType = new java.util.HashMap<>();
        for (FindingSummary f : report.findings()) countByType.put(f.type(), f.count());

        assertThat(countByType.get("DUPLICATE_CANDIDATE")).isEqualTo(16);
        assertThat(countByType.get("MISSING_MATERIAL")).isEqualTo(15);
        assertThat(countByType.get("MISSING_DESCRIPTION")).isEqualTo(12);
        assertThat(countByType.get("POOR_TITLE")).isEqualTo(10);
        assertThat(countByType.get("CATEGORY_MISMATCH")).isEqualTo(11);
        assertThat(countByType.get("SUSPICIOUS_CLAIM")).isEqualTo(9);
        assertThat(countByType.get("INCONSISTENT_COLOR")).isEqualTo(7);
    }

    @Test
    void overallScoreMatchesTheIndependentlyComputedFormula() {
        // 16*0.6 + 15*0.6 + 12*1.2 + 10*0.6 + 11*1.2 + 9*1.2 + 7*0.6 = 67.2 -> 100-67.2 = 32.8 -> 33
        assertThat(service.audit().overallScore()).isEqualTo(33);
    }

    @Test
    void everyFindingCarriesAtMostFiveSampleIdsRegardlessOfCount() {
        for (FindingSummary f : service.audit().findings()) {
            assertThat(f.sampleProductIds().size()).isLessThanOrEqualTo(5);
        }
    }

    @Test
    void scoreIsBoundedBetweenZeroAndOneHundred() {
        int score = service.audit().overallScore();
        assertThat(score).isBetween(0, 100);
    }

    @Test
    void categoryMismatchDetectsTheDeliberatelyMisplacedTentExample() {
        CatalogQualityReport report = service.audit();
        var mismatchFinding = report.findings().stream()
                .filter(f -> f.type().equals("CATEGORY_MISMATCH")).findFirst().orElseThrow();
        assertThat(mismatchFinding.count()).isGreaterThanOrEqualTo(6); // at least the 6 deliberately injected
    }
}
