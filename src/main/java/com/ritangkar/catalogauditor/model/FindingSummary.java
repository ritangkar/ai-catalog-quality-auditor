package com.ritangkar.catalogauditor.model;

import java.util.List;

/**
 * One finding type's result: how many products it flagged, a sample of
 * their ids (never the full list, to keep the report readable at catalog
 * scale), and a human-readable description of the rule that flagged them.
 */
public record FindingSummary(
        String type,
        String severity,
        int count,
        List<String> sampleProductIds,
        String description
) {
}
