package com.ritangkar.catalogauditor.service;

import com.ritangkar.catalogauditor.model.CatalogProduct;
import com.ritangkar.catalogauditor.model.FindingSummary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Each method is one independent, named, deterministic check — no LLM in
 * any of them, consistent with the master brief's requirement that the
 * validation layer stay deterministic. See docs/design-decisions.md for
 * where semantic/LLM-assisted checks could be added without touching
 * these.
 */
@Service
public class CatalogQualityChecks {

    private static final int MIN_DESCRIPTION_LENGTH = 15;
    private static final int MIN_TITLE_LENGTH = 8;
    private static final Set<String> GENERIC_TITLE_WORDS = Set.of(
            "item", "product", "stuff", "gear", "shoe", "bottle", "watch", "cap");
    private static final Pattern TITLE_CASE_COLOR = Pattern.compile("^[A-Z][a-z]+$");

    private static final Map<String, List<String>> CATEGORY_SIGNAL_WORDS = Map.of(
            "Footwear", List.of("shoe", "sneaker", "boot"),
            "Apparel", List.of("jacket", "shirt", "tee", "tights"),
            "Accessories", List.of("backpack", "bottle", "cap", "gloves"),
            "Outdoor Gear", List.of("tent", "sleeping bag", "trekking pole"),
            "Electronics", List.of("earbuds", "watch", "monitor")
    );

    private static final List<String> SUSPICIOUS_PHRASES = List.of(
            "best in the world", "guaranteed to last forever", "100% perfect", "never fails",
            "greatest", "unbeatable", "outlast any competitor", "only backpack you will ever need",
            "flawless", "the best"
    );

    /** Same name + brand, different product id — a duplicate-listing candidate. */
    public FindingSummary duplicateCandidates(List<CatalogProduct> products) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (CatalogProduct p : products) {
            groups.computeIfAbsent(p.name() + "::" + p.brand(), k -> new ArrayList<>()).add(p.id());
        }
        List<String> flagged = new ArrayList<>();
        for (var entry : groups.entrySet()) {
            if (entry.getValue().size() > 1) flagged.addAll(entry.getValue());
        }
        return summarize("DUPLICATE_CANDIDATE", "WARNING", flagged,
                "Products sharing an identical name and brand with a different product id.");
    }

    public FindingSummary missingMaterial(List<CatalogProduct> products) {
        List<String> flagged = products.stream()
                .filter(p -> p.material() == null || p.material().isBlank())
                .map(CatalogProduct::id).toList();
        return summarize("MISSING_MATERIAL", "WARNING", flagged, "Products with no material attribute set.");
    }

    public FindingSummary missingOrShortDescription(List<CatalogProduct> products) {
        List<String> flagged = products.stream()
                .filter(p -> p.description() == null || p.description().trim().length() < MIN_DESCRIPTION_LENGTH)
                .map(CatalogProduct::id).toList();
        return summarize("MISSING_DESCRIPTION", "ERROR", flagged,
                "Products with no description, or one shorter than " + MIN_DESCRIPTION_LENGTH + " characters.");
    }

    public FindingSummary poorTitles(List<CatalogProduct> products) {
        List<String> flagged = products.stream()
                .filter(p -> isPoorTitle(p.name()))
                .map(CatalogProduct::id).toList();
        return summarize("POOR_TITLE", "WARNING", flagged,
                "Titles that are too short, ALL CAPS, or a bare generic word.");
    }

    private boolean isPoorTitle(String name) {
        if (name.length() < MIN_TITLE_LENGTH) return true;
        if (name.equals(name.toUpperCase(Locale.ROOT)) && name.length() > 3) return true;
        return GENERIC_TITLE_WORDS.contains(name.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * The product's name contains a keyword strongly associated with a
     * category other than the one it's actually tagged with.
     */
    public FindingSummary categoryMismatches(List<CatalogProduct> products) {
        List<String> flagged = products.stream()
                .filter(p -> impliedCategory(p) != null)
                .map(CatalogProduct::id).toList();
        return summarize("CATEGORY_MISMATCH", "ERROR", flagged,
                "The product name implies a different category than the one it's tagged with.");
    }

    private String impliedCategory(CatalogProduct p) {
        String nameLower = p.name().toLowerCase(Locale.ROOT);
        for (var entry : CATEGORY_SIGNAL_WORDS.entrySet()) {
            if (entry.getKey().equals(p.category())) continue;
            for (String word : entry.getValue()) {
                if (nameLower.contains(word)) return entry.getKey();
            }
        }
        return null;
    }

    public FindingSummary suspiciousClaims(List<CatalogProduct> products) {
        List<String> flagged = products.stream()
                .filter(p -> p.description() != null && containsSuspiciousPhrase(p.description().toLowerCase(Locale.ROOT)))
                .map(CatalogProduct::id).toList();
        return summarize("SUSPICIOUS_CLAIM", "ERROR", flagged,
                "Descriptions containing unsupported absolute/superlative marketing claims.");
    }

    private boolean containsSuspiciousPhrase(String descriptionLower) {
        return SUSPICIOUS_PHRASES.stream().anyMatch(descriptionLower::contains);
    }

    /** Color values that aren't clean Title Case, or carry stray whitespace/punctuation. */
    public FindingSummary inconsistentColors(List<CatalogProduct> products) {
        List<String> flagged = products.stream()
                .filter(p -> isMalformedColor(p.color()))
                .map(CatalogProduct::id).toList();
        return summarize("INCONSISTENT_COLOR", "WARNING", flagged,
                "Color values that aren't clean Title Case (e.g. \"BLACK\", \" Navy\", \"white!\").");
    }

    private boolean isMalformedColor(String color) {
        if (color == null) return true;
        if (!color.equals(color.trim())) return true;
        return !TITLE_CASE_COLOR.matcher(color).matches();
    }

    private FindingSummary summarize(String type, String severity, List<String> flaggedIds, String description) {
        List<String> sample = flaggedIds.size() > 5 ? flaggedIds.subList(0, 5) : flaggedIds;
        return new FindingSummary(type, severity, flaggedIds.size(), List.copyOf(sample), description);
    }
}
