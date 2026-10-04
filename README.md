# AI Catalog Quality Auditor

Seven independent, deterministic checks over a product catalog: duplicate
candidates, missing attributes, poor titles, category mismatches, missing
descriptions, suspicious marketing claims, and inconsistent attribute
values — combined into one overall quality score, every finding traceable
to specific product ids.

Part of the [Commerce Engineering Lab](https://ritangkar.github.io/#commerce-lab) —
17 small, production-minded engineering capabilities by Ritangkar Dey.

---

## 1. Project Overview

135 synthetic products, roughly half deliberately flawed across all seven
issue types. Run the audit; get back a 0–100 score, a per-check
occurrence count, and sample product ids for each.

## 2. Problem

Catalog data quality degrades quietly — a missing attribute here, an
inconsistent color value there, a title that's just "Item" — and nobody
notices until it shows up as a bad search result or a broken filter.
Catching it needs the same handful of checks run consistently, not a
one-off manual review.

## 3. Solution

Seven named, independent, deterministic checks, each returning exactly
which products it flagged (capped to a readable 5-item sample) and why,
combined into one score that weights ERROR-severity findings (wrong
category, unsupported claims, missing description) above WARNING-severity
ones (duplicates, missing material, poor titles, inconsistent colors).

## 4. Key Features

- **Duplicate candidates**: identical name + brand, different product id
- **Missing material / missing or short description**
- **Poor titles**: too short, ALL CAPS, or a bare generic word
- **Category mismatches**: the name implies a different category than
  the one it's tagged with (e.g. a "Tent" tagged "Footwear")
- **Suspicious claims**: unsupported superlative marketing language
  ("guaranteed to last forever," "the best in the world")
- **Inconsistent colors**: not clean Title Case ("BLACK", " Navy", "white!")
- A transparent, itemized score breakdown — not just a bare number

## 5. Architecture

```
catalog.json -> CatalogRepository -> CatalogQualityChecks (7 independent checks)
                                          |
                                          v
                              CatalogAuditService (scoring)
                                          |
                                          v
                                 CatalogQualityReport
```

See [`docs/architecture.md`](docs/architecture.md) for the full diagram.

## 6. Technical Approach

Every check is deterministic — dictionaries, regexes, exact-match logic.
No LLM anywhere in this repository; see
[`docs/design-decisions.md`](docs/design-decisions.md) for where an
AI-assisted (not AI-dependent) check would plug in without touching the
deterministic baseline.

## 7. Design Decisions

See [`docs/design-decisions.md`](docs/design-decisions.md) — including an
honest note that this project's bundled catalog is a deliberately
issue-dense stress test (roughly 56% of products flagged by at least one
check), not a claim about typical real-world catalog quality.

## 8. Sample Input

```
GET /api/catalog/audit
```

## 9. Sample Output

Real output from the bundled 135-product catalog:

```json
{
  "totalProducts": 135,
  "overallScore": 33,
  "findings": [
    { "type": "DUPLICATE_CANDIDATE", "severity": "WARNING", "count": 16 },
    { "type": "MISSING_MATERIAL", "severity": "WARNING", "count": 15 },
    { "type": "MISSING_DESCRIPTION", "severity": "ERROR", "count": 12 },
    { "type": "POOR_TITLE", "severity": "WARNING", "count": 10 },
    { "type": "CATEGORY_MISMATCH", "severity": "ERROR", "count": 11 },
    { "type": "SUSPICIOUS_CLAIM", "severity": "ERROR", "count": 9 },
    { "type": "INCONSISTENT_COLOR", "severity": "WARNING", "count": 7 }
  ]
}
```

*This is a deliberately issue-dense demo catalog — see Section 7 above.*

## 10. How to Run

```bash
git clone <your-repo-url>
cd ai-catalog-quality-auditor
mvn spring-boot:run
```

API available at `http://localhost:8094`.

## 11. How to Test

```bash
mvn test
```

Every check's count is asserted against an independently-computed Python
reference value, and the overall score against the same hand-verified formula.

## 12. API Documentation

| Method | Path | Description |
|---|---|---|
| GET | `/api/catalog/audit` | The full quality report |
| GET | `/api/catalog/products` | The raw bundled catalog |

## 13. Portfolio Demo

The Commerce Engineering Lab demo (`lab/demos/ai-catalog-quality-auditor.js`)
ports all seven checks to client-side JavaScript over the same bundled
catalog. See [`docs/demo.md`](docs/demo.md).

## 14. Limitations

- Duplicate detection is exact-match only (name + brand) — near-duplicates
  with slightly different names aren't caught
- The bundled catalog is synthetic and deliberately issue-dense — not
  representative of typical catalog quality
- Category-mismatch and suspicious-claim dictionaries are hand-built and
  illustrative, not exhaustive

## 15. Future Enhancements

- Fuzzy/semantic duplicate detection (see design decisions for the seam)
- A per-product detail endpoint showing every finding that touched one product
- Configurable severity weights per deployment's priorities

## 16. License

MIT — see [`LICENSE`](LICENSE).
