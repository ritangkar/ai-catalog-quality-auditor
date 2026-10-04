# Design Decisions

---

**Decision:** Every check is deterministic Java — dictionaries, regexes,
and exact/near-exact string matching. No LLM anywhere in this repository.

**Why:** The master brief explicitly requires the validation layer stay
deterministic, while allowing "AI may assist with semantic
comparison/classification where useful." This project's checks are all in
the category deterministic rules can handle well: exact duplicate
matching, blank-field detection, length thresholds, keyword dictionaries.
None of them need semantic understanding to be effective — a category
mismatch check doesn't need to *understand* that a tent isn't footwear, it
just needs "tent" to be in the "Outdoor Gear" signal-word list and the
product tagged "Footwear."

**Trade-off:** A duplicate that uses a slightly different name ("Trail
Runner X200" vs "TrailRunner X200 ") wouldn't be caught by exact-match
duplicate detection — only truly identical name+brand pairs are flagged.

**Future:** Fuzzy/semantic duplicate detection (embedding similarity, or
even a simple edit-distance threshold) is exactly the kind of
"AI-assisted, not AI-dependent" addition the master brief anticipates —
it would sit alongside `duplicateCandidates()`, not replace the exact-match
check, so the deterministic baseline keeps working even if the semantic
layer is unavailable.

---

**Decision:** Build a dedicated synthetic catalog with deliberately
injected issues for this project, rather than solely auditing the Smart
Product Search Engine project's existing catalog.

**Why:** A rich, clearly-labeled demonstration needs enough real issues of
every type to be worth showing — 135 products with roughly half
deliberately flawed across all seven categories, rather than hoping
enough incidental issues exist in a catalog built for a different purpose.
(Coincidentally, the Smart Product Search Engine catalog *does* have a
real duplicate-name issue of its own, noted in the Personalized
Recommendation Engine project's design decisions — this auditor would
catch that one too, if pointed at it.)

**Trade-off:** This project's bundled catalog is synthetic and
deliberately dense with issues (roughly 56% of products have at least one
flagged problem) — a real catalog would very likely score higher. The
resulting overall score (33/100) is a stress-test result, not a
claim about typical real-world catalog quality, and is documented as such
rather than smoothed toward a more flattering number.

---

**Decision:** Report each check's finding count honestly, even where a
check finds more than was deliberately injected for it.

**Why:** `CATEGORY_MISMATCH` finds 11 products, not the 6 deliberately
constructed for it — several of the deliberately-poor-titled products
(e.g., a lone word like "Watch" or "Bottle" assigned a random category)
also happen to imply a different category than they were randomly
assigned, which is itself a realistic finding: a product with one quality
problem (a bad title) is plausibly more likely to have another (a
mismatched category) in a genuinely messy catalog. Reporting the honest
count, and explaining why it's higher than the "planned" number, is more
useful than trimming it to match expectations.
