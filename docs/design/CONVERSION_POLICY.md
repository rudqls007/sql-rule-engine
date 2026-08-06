# Conversion Policy

**Version**: v0.3+  
**Focus**: Specific rule definitions, confidence levels, validation criteria  
**Audience**: Rule authors, QA engineers, database architects  

---

## 1. Confidence Level Framework

Every conversion has a **confidence score** (0-100%) that indicates how likely the conversion is to be correct.

### 1.1 Confidence Bands

```
90-100% ███████████ HIGH
        Automatic deployment, no review needed
        Example: NVL → COALESCE

70-89%  ▓▓▓▓▓▓▓▓▓░  MEDIUM  
        AI review recommended, test before deploy
        Example: DECODE → CASE WHEN

30-69%  ▓▓▓▓░░░░░░  LOW
        Manual review required, human decides
        Example: DBMS_OUTPUT → PostgreSQL logging

0-29%   ░░░░░░░░░░  UNSUPPORTED
        No automatic conversion, manual work only
        Example: Oracle clustering strategies
```

### 1.2 What Drives Confidence

Confidence score is determined by:

| Factor | Weight | Impact |
|--------|--------|--------|
| **Test Coverage** | 40% | More tests = higher confidence |
| **Equivalence Proof** | 30% | Can we prove input ≈ output? |
| **Performance Impact** | 15% | Speed regression affects confidence |
| **Edge Case Handling** | 10% | Known limitations reduce score |
| **Historical Success** | 5% | Past conversions on target DB |

### 1.3 Confidence Calculation

```
Base Confidence = 100%

Adjustments:
├─ Test Coverage:
│  ├─ <10 tests:        -40% → 60%
│  ├─ 10-50 tests:      -20% → 80%
│  ├─ 50-100 tests:     -5%  → 95%
│  └─ >100 tests:       +0%  → 100%
│
├─ Equivalence Proof:
│  ├─ No proof:         -30% (uncertain)
│  ├─ Partial proof:    -15% (likely equivalent)
│  └─ Full proof:       +0%  (proven equivalent)
│
├─ Performance Impact:
│  ├─ >20% regression:  -15% (significant impact)
│  ├─ 10-20% slower:    -5%  (moderate impact)
│  └─ <10% or faster:   +0%  (acceptable)
│
├─ Edge Cases:
│  ├─ Many known cases: -10% (complex)
│  ├─ Few edge cases:   -5%  (simple)
│  └─ No edge cases:    +0%  (straightforward)
│
└─ Historical Success:
   ├─ <80% pass rate:   -5%  (some failures)
   └─ >80% pass rate:   +0%  (generally reliable)

Final Confidence = Base - Adjustments
Range: 0-100%
```

**Example Calculation**:
```
Rule: NVL(a, b) → COALESCE(a, b)

Base:                    100%
- Test coverage:         -0%   (200 tests ✓)
- Equivalence:           -0%   (proven equivalent ✓)
- Performance:           -0%   (identical ✓)
- Edge cases:            -0%   (no edge cases ✓)
- Historical:            -0%   (99% pass rate ✓)

Final Confidence:        100% → Display as 95% (conservative)
                         → TIER 1 (AUTOMATIC)
```

---

## 2. Rule Categories & Confidence Baselines

### 2.1 Function Conversion Rules

**Category**: Functions that map directly between Oracle and target

**Base Confidence**: 80% (functions have clear equivalents)

#### NVL Rules

**Rule: NVL(x, y) → COALESCE(x, y)**

| Metric | Value |
|--------|-------|
| **Source DB** | Oracle |
| **Target DBs** | PostgreSQL, MySQL, MSSQL, eXperDB |
| **Confidence** | **95%** (HIGH) → TIER 1 |
| **Test Count** | 200+ real functions |
| **Performance** | Identical |
| **Edge Cases** | None known |

```
✓ Input:  NVL(col, 'N/A')
✓ Output: COALESCE(col, 'N/A')

✓ Input:  NVL(NULL, 'default')
✓ Output: COALESCE(NULL, 'default') 

✗ Input:  NVL(SUBSTR(...), default)  [Regex limitation]
✗ Output: Not captured by regex rule
→ Deferred to v0.4 parser
```

**Validation**:
- [x] Syntax: Output is valid SQL
- [x] Types: COALESCE preserves types
- [x] Semantics: Returns second arg if first is NULL
- [x] Performance: O(n) same as NVL

---

**Rule: NVL2(x, y, z) → CASE WHEN (or COALESCE variant)**

| Metric | Value |
|--------|-------|
| **Source DB** | Oracle |
| **Target DBs** | PostgreSQL, MySQL, MSSQL |
| **Confidence** | **85%** (MEDIUM) → TIER 2 |
| **Test Count** | 50-75 cases |
| **Performance** | +5-10% slower (extra CASE evaluation) |
| **Edge Cases** | NULL in else clause |

```
Oracle:   NVL2(x, y, z)     -- IF x NOT NULL THEN y ELSE z
PostgreSQL: CASE WHEN x IS NOT NULL THEN y ELSE z END

✓ Input:  NVL2(col, 'exists', 'missing')
✓ Output: CASE WHEN col IS NOT NULL THEN 'exists' ELSE 'missing' END

⚠ Input:  NVL2(NULL, 'Y', NULL)
⚠ Output: Returns NULL (correct, but less obvious in PostgreSQL)
→ May surprise developers unfamiliar with CASE WHEN semantics
```

**Validation**:
- [x] Syntax: Valid SQL
- [⚠] Types: NULL handling differs slightly
- [x] Semantics: Functionally equivalent
- [⚠] Performance: Slightly slower due to CASE overhead

**Confidence Reduction**: -10% due to performance and NULL semantics clarity

---

#### DECODE Rule

**Rule: DECODE(...) → CASE WHEN ... THEN ... END**

| Metric | Value |
|--------|-------|
| **Source DB** | Oracle |
| **Target DBs** | PostgreSQL, MySQL, MSSQL |
| **Confidence** | **78%** (MEDIUM) → TIER 2 |
| **Test Count** | 40-50 cases |
| **Performance** | -12% slower (CASE evaluation overhead) |
| **Edge Cases** | NULL handling, equality semantics |

```
Oracle:   DECODE(x, a, 1, b, 2, 3)       -- IF x=a THEN 1 ELIF x=b THEN 2 ELSE 3
PostgreSQL: CASE WHEN x=a THEN 1 WHEN x=b THEN 2 ELSE 3 END

✓ Input:  DECODE(status, 'A', 1, 'B', 2, 0)
✓ Output: CASE WHEN status='A' THEN 1 WHEN status='B' THEN 2 ELSE 0 END

⚠ Input:  DECODE(NULL, NULL, 'match', 'no-match')
⚠ Oracle:  Returns 'match' (NULL = NULL in DECODE)
⚠ PostgreSQL: Returns 'no-match' (NULL != NULL in standard SQL)
→ CRITICAL: Need explicit IS NULL check
```

**Validation**:
- [x] Syntax: Valid SQL
- [⚠] Types: Compatible
- [✗] Semantics: Different NULL equality semantics (Oracle DECODE treats NULL=NULL as true)
- [⚠] Performance: -12% slower

**Confidence Reduction**: -22% due to NULL handling difference (-15%) and performance (-7%)

**Recommendation**:
```
Option A (Default, higher confidence):
  CASE WHEN x IS NULL AND a IS NULL THEN result
       WHEN x = a THEN result
       ...
  END

Option B (Simpler, assumes non-NULL):
  CASE WHEN x = a THEN result
       ...
  END
→ Use only if NULL values not possible in data
```

---

### 2.2 Date/Time Conversion Rules

**Category**: Date and time functions (often have platform differences)

**Base Confidence**: 75% (date behavior is tricky across platforms)

#### SYSDATE Rule

**Rule: SYSDATE → CURRENT_TIMESTAMP**

| Metric | Value |
|--------|-------|
| **Source DB** | Oracle |
| **Target DBs** | PostgreSQL, MySQL, MSSQL |
| **Confidence** | **98%** (HIGH) → TIER 1 |
| **Test Count** | 150+ uses |
| **Performance** | Identical |
| **Edge Cases** | Timezone handling (v0.6) |

```
✓ Input:  INSERT INTO logs VALUES(SYSDATE)
✓ Output: INSERT INTO logs VALUES(CURRENT_TIMESTAMP)

✓ Input:  SELECT TRUNC(SYSDATE) ...
✓ Output: SELECT DATE_TRUNC('day', CURRENT_TIMESTAMP) ...

⚠ Timezone: Oracle default is session timezone
           PostgreSQL default is client timezone
           → Handled separately by timezone rule
```

**Validation**:
- [x] Syntax: Valid SQL
- [x] Types: Both return TIMESTAMP
- [x] Semantics: Both return current date/time
- [x] Performance: Identical

---

#### TRUNC (Date) Rule

**Rule: TRUNC(date, format) → DATE_TRUNC(format, date)**

| Metric | Value |
|--------|-------|
| **Source DB** | Oracle |
| **Target DBs** | PostgreSQL, MySQL (partial) |
| **Confidence** | **88%** (MEDIUM) → TIER 2 |
| **Test Count** | 60+ cases |
| **Performance** | -8% slower (function call overhead) |
| **Edge Cases** | Format string differences |

```
Oracle formats:  TRUNC(date, 'YYYY'), TRUNC(date, 'MM'), TRUNC(date, 'DD')
PostgreSQL:      DATE_TRUNC('year', date), DATE_TRUNC('month', date)
MySQL:           DATE(), LAST_DAY(), DATE_FORMAT() [multiple functions]

✓ Input:  TRUNC(p_date, 'YYYY')
✓ Output: DATE_TRUNC('year', p_date)

✓ Input:  TRUNC(p_date, 'MM')
✓ Output: DATE_TRUNC('month', p_date)

⚠ Input:  TRUNC(p_date, 'Q')    [Quarter]
⚠ Output: Not supported in PostgreSQL DATE_TRUNC
→ Fallback: EXTRACT and manual calculation
```

**Validation**:
- [x] Syntax: Valid (for supported formats)
- [⚠] Types: Both return DATE/TIMESTAMP
- [x] Semantics: Both truncate to specified unit
- [⚠] Performance: Slightly slower

**Supported Formats**:
- [x] 'YYYY' → 'year'
- [x] 'MM' → 'month'
- [x] 'DD' → 'day'
- [⚠] 'Q' → Not supported (manual calc)
- [x] 'HH' → 'hour'
- [x] 'MI' → 'minute'

**Confidence Reduction**: -12% due to format limitations and performance

---

### 2.3 String Conversion Rules

**Category**: String manipulation (usually direct equivalents)

**Base Confidence**: 85% (most string functions are stable)

#### SUBSTR Rule

**Rule: SUBSTR(str, start, len) → SUBSTRING(str, start, len)**

| Metric | Value |
|--------|-------|
| **Source DB** | Oracle |
| **Target DBs** | PostgreSQL, MySQL, MSSQL |
| **Confidence** | **96%** (HIGH) → TIER 1 |
| **Test Count** | 100+ functions |
| **Performance** | Identical |
| **Edge Cases** | 1-based indexing (handled by conversion) |

```
Oracle uses 1-based indexing:  SUBSTR('hello', 1, 3) → 'hel'
PostgreSQL uses 1-based:       SUBSTRING('hello', 1, 3) → 'hel'  ✓

✓ Input:  SUBSTR(col, 1, 10)
✓ Output: SUBSTRING(col, 1, 10)

✗ Input:  SUBSTR(SUBSTR(col, 1, 5), 1, 3)  [Nested, regex limitation]
✗ Output: Not captured by regex
→ Deferred to v0.4 parser
```

**Validation**:
- [x] Syntax: Valid SQL
- [x] Types: Both return VARCHAR
- [x] Semantics: Both extract substring
- [x] Performance: Identical

**Note**: Negative indexing in Oracle not supported (rare use case)

---

#### LENGTH Rule

**Rule: LENGTH(str) → LENGTH(str)  [Passthrough with validation]**

| Metric | Value |
|--------|-------|
| **Source DB** | Oracle |
| **Target DBs** | PostgreSQL, MySQL, MSSQL |
| **Confidence** | **99%** (HIGH) → TIER 1 |
| **Test Count** | 80+ uses |
| **Performance** | Identical |
| **Edge Cases** | NULL handling (same everywhere) |

```
✓ Input:  LENGTH(col)
✓ Output: LENGTH(col)

⚠ Oracle: LENGTH of NULL is NULL (standard SQL)
✓ PostgreSQL: LENGTH of NULL is NULL (same)

Note: Oracle's LENGTH returns characters, not bytes
      PostgreSQL's LENGTH returns characters
      For bytes, use OCTET_LENGTH (different need)
```

**Validation**:
- [x] Syntax: Valid SQL
- [x] Types: Both return INTEGER
- [x] Semantics: Both return string length
- [x] Performance: Identical

**Action**: Direct passthrough, no modification needed

---

### 2.4 System Conversion Rules

**Category**: Database-specific system functions (hard to map)

**Base Confidence**: 50% (often no direct equivalent)

#### DBMS_OUTPUT Rule

**Rule: DBMS_OUTPUT.PUT_LINE(msg) → PostgreSQL RAISE/LOG**

| Metric | Value |
|--------|-------|
| **Source DB** | Oracle PL/SQL |
| **Target DBs** | PostgreSQL (plpgsql) |
| **Confidence** | **35%** (LOW) → TIER 3 |
| **Test Count** | 20 cases |
| **Performance** | Different mechanism (async log) |
| **Edge Cases** | Output buffering, flush behavior |

```
Oracle:       DBMS_OUTPUT.PUT_LINE('message');
PostgreSQL:   RAISE NOTICE 'message';  -- synchronous
            or: RAISE LOG 'message';    -- asynchronous

⚠ Behavior difference:
  - Oracle: Buffers output, client fetches
  - PostgreSQL: Logs to server log (if RAISE LOG)
  - PostgreSQL: Shows in client console (if RAISE NOTICE)

⚠ Cannot automatically choose between NOTICE or LOG
→ Manual decision based on context
```

**Validation**:
- [⚠] Syntax: Valid PL/pgSQL (but different semantics)
- [✗] Semantics: Fundamentally different (buffered vs logged)
- [✗] Performance: Different mechanism entirely

**Recommendation**:
```
1. ANALYZE: Is this for debugging or application output?
   → If debugging: Use RAISE NOTICE (shown during development)
   → If logging: Use RAISE LOG (appears in server log)

2. REDESIGN: Consider using application logging instead
   → PostgreSQL favors application-level logging
   → Database-level RAISE is for exceptions, not general logging

3. MIGRATE: Provide both options to developer
   - Option A: RAISE NOTICE (console friendly)
   - Option B: RAISE LOG (server log friendly)
```

**Confidence**: Cannot auto-convert, requires human decision → 35% (TIER 3)

---

## 3. Validation Rules Framework

Every conversion is validated against a comprehensive checklist.

### 3.1 Syntax Validation

**Purpose**: Ensure output is valid SQL for target database

**Checks**:
- [ ] Valid SQL keywords for target DB
- [ ] Balanced parentheses
- [ ] Proper function call syntax
- [ ] Reserved keyword handling
- [ ] String literal escaping

**Example**:
```sql
Input:  NVL(col, 'O''Brien')  -- Oracle escaped quote
Check:  Does PostgreSQL handle ''  correctly?
Output: COALESCE(col, 'O''Brien')  -- PostgreSQL same escape ✓
Result: ✓ PASS
```

### 3.2 Type Validation

**Purpose**: Ensure data types are compatible between input and output

**Checks**:
- [ ] Function return type matches expected
- [ ] Parameter types are compatible
- [ ] Type coercion is explicit (not implicit)
- [ ] NULL value handling is type-safe

**Example**:
```sql
Input:  NVL(numeric_col, 'text')  -- Type mismatch
Check:  Oracle allows implicit coercion
Output: COALESCE(numeric_col, 'text')  -- PostgreSQL requires explicit
Result: ✗ FAIL (flag for manual review)
Fix:    COALESCE(numeric_col, CAST('text' AS NUMBER))
```

### 3.3 Semantic Validation

**Purpose**: Ensure logic is equivalent between input and output

**Checks**:
- [ ] Function behavior is identical
- [ ] NULL handling is the same
- [ ] Edge cases produce same results
- [ ] No silent behavior changes

**Example**:
```sql
Input:  DECODE(x, NULL, 'found', 'not')
Oracle:  x=NULL: 'found' (NULL=NULL is true in DECODE)
PostgreSQL: x=NULL: 'not' (NULL=NULL is false in SQL)

Result: ✗ FAIL (semantics differ)
Fix:    CASE WHEN x IS NULL THEN 'found' ELSE 'not' END
```

### 3.4 Performance Validation

**Purpose**: Ensure converted SQL doesn't have unacceptable performance regression

**Acceptable Performance Impact**: ±10%

**Validation**:
- [ ] Query execution time measured
- [ ] Index utilization unchanged
- [ ] Memory usage acceptable
- [ ] No full table scans introduced

**Example**:
```
Oracle execution: 100ms
PostgreSQL converted: 110ms  (+10%)

Result: ✓ PASS (within acceptable range)
```

If regression > ±10%:
```
Oracle execution: 100ms
PostgreSQL converted: 150ms  (+50%)

Result: ✗ FAIL (downgrade confidence)
Action: Optimize or redesign
```

### 3.5 Dependency Validation (v0.6+)

**Purpose**: Ensure converted SQL doesn't reference undefined objects

**Checks**:
- [ ] Called functions exist in target schema
- [ ] Referenced tables exist
- [ ] Column names are correct
- [ ] No forward references to undefined objects

**Example**:
```sql
Input:  SELECT calc_days_difference(start, end) FROM events
Check:  Does calc_days_difference() exist in PostgreSQL?

If migrating functions together:
  ✓ PASS (function exists after migration)
  
If function not migrated:
  ✗ FAIL (function undefined)
  Action: Mark as blocked until function is migrated
```

---

## 4. Conversion Result Format

Every conversion produces a **ConversionResult** object with complete metadata.

### 4.1 Result Structure

```json
{
  "metadata": {
    "object_id": "calc_days_difference",
    "object_type": "FUNCTION",
    "source_database": "ORACLE",
    "target_database": "POSTGRESQL",
    "conversion_timestamp": "2026-08-06T14:01:10Z"
  },

  "conversion": {
    "input_code": "CREATE FUNCTION calc_days_difference(p_start_date IN DATE, p_end_date IN DATE) RETURN NUMBER AS ...",
    "output_code": "CREATE FUNCTION calc_days_difference(p_start_date IN TIMESTAMP, p_end_date IN TIMESTAMP) RETURNS NUMERIC AS ...",
    "status": "SUCCESS",
    "conversion_method": "RULE_ENGINE"
  },

  "confidence": {
    "overall_score": 0.78,
    "level": "MEDIUM",
    "reason": "Performance regression >10%"
  },

  "validation": {
    "syntax": {
      "status": "PASS",
      "details": "Valid PostgreSQL syntax"
    },
    "types": {
      "status": "PASS",
      "details": "Data types are compatible"
    },
    "semantics": {
      "status": "PASS",
      "details": "Logic is equivalent"
    },
    "performance": {
      "status": "WARN",
      "oracle_time_ms": 100,
      "postgresql_time_ms": 150,
      "regression_percent": -33,
      "details": "Regression exceeds ±10% threshold"
    }
  },

  "rules_applied": [
    {
      "rule_id": "TRUNC_DATE_CONVERSION",
      "rule_name": "TRUNC(date) → DATE_TRUNC",
      "priority": 100,
      "confidence": 0.88,
      "applied_count": 2
    },
    {
      "rule_id": "DATE_ARITHMETIC",
      "rule_name": "Oracle date arithmetic → PostgreSQL",
      "priority": 90,
      "confidence": 0.85,
      "applied_count": 1
    }
  ],

  "recommendations": [
    "Run performance tests before production deployment",
    "Consider query optimization (indexes, explain analyze)",
    "Verify date_trunc behavior with actual data"
  ],

  "action_required": {
    "type": "TESTING_REQUIRED",
    "level": "MEDIUM",
    "estimated_hours": 2.0,
    "description": "Performance regression requires testing and possible optimization"
  }
}
```

### 4.2 Result Status Values

| Status | Meaning | Action |
|--------|---------|--------|
| **SUCCESS** | Conversion complete, no issues | Deploy (Tier 1 only) |
| **SUCCESS_WITH_WARNINGS** | Conversion complete, minor issues | Test before deploy (Tier 2) |
| **SUCCESS_WITH_CONCERNS** | Conversion complete, significant issues | Manual review (Tier 3) |
| **PARTIAL** | Partial conversion, some parts failed | Manual redesign (Tier 3+) |
| **UNSUPPORTED** | No conversion available | Manual work (Tier 4) |
| **FAILED** | Conversion attempted but failed | Debug or manual |

---

## 5. Manual Processing Criteria

### 5.1 When to Escalate to Manual

**Escalation Rules**:

| Condition | Action | Rationale |
|-----------|--------|-----------|
| Confidence < 70% | → Manual review | Too risky to automate |
| Validation FAIL | → Manual review | Output is incorrect |
| Performance regression > ±10% | → Test queue then decide | Needs performance analysis |
| Edge case detected | → Downgrade confidence | Reduce certainty |
| Dependency missing | → Block and flag | Cannot deploy safely |
| Multiple warnings | → Manual review | Complexity too high |

### 5.2 Manual Work Estimation

**Hours to complete manual review**:

| Tier | Confidence | Typical Hours | Why |
|------|------------|---------------|-----|
| **Tier 2** | 70-90% | 0.5-1.0 | Quick spot-check |
| **Tier 3** | 30-70% | 2-4 | Design + test |
| **Tier 4** | 0-30% | 4-8 | Full redesign + test |

---

## 6. Migration Result Summary

After all conversions complete, generate **Migration Summary**:

```json
{
  "migration_summary": {
    "total_objects": 280,
    "migration_start_time": "2026-08-06T00:00:00Z",
    "migration_end_time": "2026-08-06T14:01:10Z",
    "total_duration_minutes": 841,
    "success_rate": 0.954
  },

  "tier_breakdown": {
    "tier_1_automatic": {
      "count": 200,
      "percentage": 71.4,
      "average_confidence": 0.96,
      "status": "READY_TO_DEPLOY"
    },
    "tier_2_ai_review": {
      "count": 60,
      "percentage": 21.4,
      "average_confidence": 0.81,
      "status": "READY_TO_TEST"
    },
    "tier_3_manual": {
      "count": 15,
      "percentage": 5.4,
      "average_confidence": 0.45,
      "status": "AWAITING_REVIEW"
    },
    "tier_4_unsupported": {
      "count": 5,
      "percentage": 1.8,
      "average_confidence": 0.0,
      "status": "MANUAL_WORK_ONLY"
    }
  },

  "effort_summary": {
    "automated_hours": 2.5,
    "testing_hours": 40.0,
    "manual_hours": 60.0,
    "total_effort_hours": 102.5,
    "hours_saved_vs_manual": 697.5,
    "efficiency_improvement": "87%"
  },

  "quality_metrics": {
    "validation_pass_rate": 0.96,
    "performance_regression_count": 3,
    "edge_cases_discovered": 2,
    "estimated_test_pass_rate": 0.92
  }
}
```

---

## 7. Conclusion

Conversion Policy provides:

1. **Confidence Framework** — Transparent, calculable confidence scores
2. **Rule Definitions** — Specific rules with test counts and limitations
3. **Validation Checklist** — Comprehensive checks before deployment
4. **Result Format** — Complete metadata for each conversion
5. **Manual Escalation** — Clear criteria for human review

This ensures that **every conversion** is:
- ✓ Documented
- ✓ Tested
- ✓ Validated
- ✓ Scored
- ✓ Actionable

**Next Reading**:
- `MIGRATION_STRATEGY.md` — Enterprise workflow and risk management
- `AI_INTEGRATION_PLAN.md` — How AI augments rule engine
