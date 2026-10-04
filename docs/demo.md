# Portfolio Demo

**Demo level:** 2 — Portfolio simulation (bundled sample data, client-side).

`lab/demos/ai-catalog-quality-auditor.js` ports all seven checks to
client-side JavaScript over the exact same bundled 135-product catalog, so
a recruiter sees the identical overall score, per-check counts, and
sample flagged product ids this README documents — live, computed in the
browser. No backend call.

The real Spring Boot API (`mvn spring-boot:run`) exposes the full
`/api/catalog/audit` and `/api/catalog/products` surface for inspecting
every flagged product in detail, not just the 5-item samples the report
returns by default.
