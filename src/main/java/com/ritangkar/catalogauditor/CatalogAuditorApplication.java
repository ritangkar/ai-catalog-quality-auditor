package com.ritangkar.catalogauditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Catalog Quality Auditor.
 *
 * Seven independent, deterministic checks over a product catalog:
 * duplicate candidates, missing attributes, poor titles, category
 * mismatches, missing descriptions, suspicious marketing claims, and
 * inconsistent attribute values — each producing a named finding type
 * with sample product ids, combined into one overall quality score.
 * Nothing here depends on an LLM; see docs/design-decisions.md.
 */
@SpringBootApplication
public class CatalogAuditorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogAuditorApplication.class, args);
    }
}
