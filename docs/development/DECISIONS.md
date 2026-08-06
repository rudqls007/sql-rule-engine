# Architecture Decision Records (ADRs)

This document records important design decisions and their rationale.

---

## ADR-001: Why Java 21?

**Decision**: Use Java 21 as the primary language.

**Rationale**:
- Modern, stable, widely used in enterprise migration projects
- Strong ecosystem for text processing and parsing
- Type safety catches SQL conversion errors early
- Long-term support (LTS version) ensures stability
- Excellent IDE tooling for large codebases
- Good candidate base for future open-source contributions

**Alternatives Considered**:
- **Python**: Easier regex/parsing, but less suitable for enterprise frameworks that need to scale to 1000+ objects
- **Go**: Simpler deployment, but smaller ecosystem for SQL tools

**Impact**:
- Architecture decisions (parsing, type systems) will follow Java conventions
- Build tool choice (Gradle) is natural fit
- Testing framework (JUnit 5) integrates well

**Status**: ✅ Accepted (v0.1)

---

## ADR-002: Why Rule Pattern Instead of Visitor/Compiler?

**Decision**: Use a simple Rule pattern (supports/convert) for v0.1 instead of building an AST-based compiler.

**Rationale for v0.1**:
- Enables working rules in days, not weeks
- Low complexity: New developers understand instantly
- High clarity: Rule abstraction is self-explanatory
- Extensible: Easy to add new regex-based rules
- Iterative: Can graduate to AST when needed

**Why Not AST from Day 1?**
- Would violate "Practical before Perfect" principle
- Would take 3x longer to ship working rules
- Requires parser first (planned for v0.4)
- Better to learn from 10+ working regex rules than hypothetical patterns

**Limitations Acknowledged** (intentional):
- Cannot handle complex nested logic (e.g., NVL(SUBSTR(...), b))
- Regex becomes unmaintainable at scale
- No semantic understanding of SQL

**Future Plan**:
- v0.3: Identify patterns from 10+ rules
- v0.4: Build proper SQL parser and AST
- v0.4+: Migrate rules to AST-based transformations

**Status**: ✅ Accepted (v0.1-v0.3), Will Revisit (v0.4+)

---

## ADR-003: Why This Package Structure?

**Decision**: Organize packages by responsibility (rule/, engine/, parser/) not by database or feature.

**Rationale**:
- Encourages single-responsibility principle
- Clear home for new code (where do I add a new rule? rule/impl/)
- Aligns with future architecture layers
- Scales better than feature-based organization
- Prevents vendor lock-in (oracle/, postgresql/ packages)

**Structure Decision**:
```
rule/          → All rule abstractions and implementations
engine/        → Orchestration: registry, engine
parser/        → (Future) SQL parsing
converter/     → (Future) AST transformation
generator/     → (Future) Target-specific SQL generation
validator/     → (Future) Validation and reporting
```

**Alternatives Considered**:
- **Feature-based** (oracle/, postgresql/, mysql/): Creates early vendor lock-in, doesn't scale
- **Monolithic** (all in one package): Fine for <10 rules, chaotic for 100+ rules

**Impact**:
- Every developer knows where to add new code
- Package structure is stable, survives major refactoring
- Makes it easy to move from regex rules → AST rules (both in same package)

**Status**: ✅ Accepted (v0.1+)

---

## ADR-004: Why Sequential Pipeline (Not Priority-Based)?

**Decision**: RuleEngine applies rules in registration order, single pass, no priorities.

**Rationale for v0.1**:
- Simplest implementation
- Sufficient for initial rules (only 1-2 rules in v0.1)
- Easy to test and reason about
- Predictable execution order
- Can add priorities/phases later without breaking API

**Known Issues** (accepted):
- Rule order matters (could cause bugs if not careful)
- No conflict detection
- No rollback on failure

**Example Problem** (Future):
```
If Rule A depends on output from Rule B:
  engine.register(new RuleA());
  engine.register(new RuleB());  // Wrong order!
  
Rule A might see un-transformed SQL
```

**Future Solution** (v0.2+):
- Add rule metadata (priority, dependencies)
- RuleEngine respects priorities
- Add conflict detection

**Status**: ✅ Accepted with known limitations (v0.1), Will Improve (v0.2+)

---

## ADR-005: Why NullPointerException for Null Rules?

**Decision**: RuleRegistry throws NullPointerException on null rule registration.

**Rationale**:
- Fail-fast principle: catch errors immediately
- Prevents silent rule registration failures
- Aligns with Java conventions (NPE for null references)
- Unusual to register null rule (programmer error, not normal case)

**Example**:
```java
registry.register(null);  // Throws NullPointerException immediately
// vs.
registry.register(null);  // Silently fails, discovered later
```

**Future Consideration** (v0.2+):
- Introduce SqlRuleException for domain-specific errors
- Keep NullPointerException for programming errors
- Add custom exception hierarchy

**Status**: ✅ Accepted (v0.1)

---

## ADR-006: Why Regex-Based NvlToCoalesceRule?

**Decision**: First rule uses regex, not a parser.

**Rationale**:
- Ships working rule immediately
- Good enough for 80% of simple cases
- Allows v0.1-v0.3 to focus on rule engine patterns
- Parser comes in v0.4 (after we have 10+ rules to test against)
- Builds confidence in the system quickly

**Acknowledged Limitations**:
- Cannot handle nested parentheses: NVL(SUBSTR(a, 1, 3), b)
- Cannot handle functions with commas: NVL(CASE WHEN ... END, b)
- May fail on complex expressions

**Success Cases**:
- ✅ `NVL(a, b)`
- ✅ `NVL(col1, 'N/A')`
- ✅ `NVL(price, 0)`

**Failure Cases**:
- ❌ `NVL(SUBSTR(col, 1, 3), 'N/A')` (nested function)
- ❌ `NVL(CASE WHEN x > 0 THEN y ELSE z END, b)` (commas inside)

**When Will We Fix This?**:
- v0.4: Proper SQL parser replaces regex entirely
- All v0.1-v0.3 regex rules will be migrated to AST-based rules

**Status**: ✅ Accepted (v0.1-v0.3), Will Replace (v0.4)

---

## ADR-007: Why No Custom Exceptions Yet?

**Decision**: Current code uses standard Java exceptions, no custom exception hierarchy.

**Rationale**:
- Over-engineering: Custom exceptions not needed until v0.2
- Practical before Perfect: Focus on rule patterns first
- Exception design can be done when we have 5+ exception types
- Rule engine doesn't fail — rules are stateless, always return result

**Where Exceptions Would Help** (Future):
- Parser: ParseException (malformed SQL)
- Validator: ValidationException (unsupported syntax)
- Rule: RuleException (rule execution failed)
- Converter: ConversionException (transformation failed)

**Current Approach**:
- NullPointerException: Programming error (passing null rule)
- (Other operations don't throw — rules are safe)

**Future Plan** (v0.2+):
- Design SqlRuleException hierarchy
- Reserve custom exceptions for actual domain errors
- Keep NPE for programming errors

**Status**: Pending (v0.2+)

---

## ADR-008: Why Focus on Enterprise Migrations (Not Toy Examples)?

**Decision**: Build for real Oracle → PostgreSQL migrations (stored procedures, functions, packages, triggers), not toy SQL examples.

**Rationale**:
- Solves real problems that companies face today
- Architecture must scale to 100s of objects, 10+ databases
- Enterprise context informs better design decisions
- Builds trust: "This was built for real migrations"

**Impact on Design**:
- Package structure anticipates future components (parser, generator, validator)
- RuleEngine is just foundation; pipeline will be more sophisticated
- Confidence scoring and reporting are non-negotiable
- Dependencies between objects must be tracked (v0.5+)

**How It Influences v0.1**:
- Clear separation between rule engine and future components
- RuleRegistry pattern supports future complexity
- Documentation reflects enterprise use cases
- Testing considers production-scale scenarios

**What This Is NOT**:
- Not a tutorial project (no "hello world" simplicity)
- Not a toy to learn Gradle (real dependencies, real problems)
- Not a coding exercise (production mindset)

**Status**: ✅ Accepted (v0.1+)

---

## ADR-009: Why Confidence Scoring Matters (Future - v0.6+)

**Decision**: Every conversion will include a confidence score (HIGH/MEDIUM/LOW) once validation is added.

**Rationale**:
- Enterprise teams need to know which conversions are reliable
- Some conversions are straightforward (NVL → COALESCE = HIGH)
- Some are complex (DBMS_OUTPUT → PostgreSQL logging = MEDIUM)
- Some are impossible without manual work (proprietary extensions = LOW/FAIL)
- Enables risk-based migration planning

**Future Confidence Scoring** (v0.6+):
```
HIGH (>85%):    Direct equivalent exists, well-tested
MEDIUM (50-85%): Equivalent exists but may need tuning
LOW (<50%):     Workaround required, manual verification needed
FAIL (0%):      No automatic conversion, manual work required
```

**Example**:
```
NVL(a, b) → COALESCE(a, b)      [HIGH: 95% confidence]
DECODE(...) → CASE WHEN...       [MEDIUM: 75% confidence, needs testing]
DBMS_OUTPUT.PUT_LINE → ???       [LOW: 30%, requires manual design]
Proprietary Oracle → N/A         [FAIL: 0%, must be rewritten]
```

**Why Not in v0.1?**
- Need validator first (v0.6)
- Only one rule exists (no scoring needed yet)
- Better to establish pattern after 10+ rules exist

**Migration Report Impact** (v0.7+):
```
Summary:
- Total objects: 100
- HIGH confidence: 72 (72%)
- MEDIUM confidence: 20 (20%)
- LOW confidence: 5 (5%)
- FAIL (manual): 3 (3%)
- Estimated effort: 72 objects automated, 8-10 hours manual work
```

**Status**: Planned (v0.6+)

---

## ADR-010: Why Multi-Database Design Now? (Future - v0.5+)

**Decision**: Design for PostgreSQL, MySQL, MSSQL, eXperDB support from v0.5.

**Rationale**:
- Don't want to redesign after shipping v0.5 with PostgreSQL only
- Same rules should work for all targets
- AST-based approach (v0.4+) allows different code generation per target
- Supports enterprise pattern: migrate same system to multiple targets

**How It Works** (v0.5+):
```
Same Oracle SQL + Same Rules → Same AST
                   ↓
         Different Generators
         ↙        ↓        ↘
    PostgreSQL  MySQL    MSSQL
```

**Example**:
```
Oracle: NVL(x, 'N/A')
   ↓ (via rules)
AST: FUNCTION[COALESCE, x, 'N/A']
   ↓ (via target-specific generator)
PostgreSQL:  COALESCE(x, 'N/A')
MySQL:       COALESCE(x, 'N/A')
MSSQL:       ISNULL(x, 'N/A')
```

**Why Build This in v0.5 (Not Later)?**:
- AST already designed in v0.4
- Prevents v0.5 redesign when MySQL target requested
- Each target DB is independent module
- Future targets (eXperDB) slot in naturally

**Status**: Planned (v0.5+)

---

## ADR-011: Why Roadmap is Enterprise-Focused?

**Decision**: Publish realistic roadmap targeting enterprise migration framework, not toy demo.

**Rationale**:
- Sets clear expectations for contributors
- Prevents scope creep (stay focused on foundation)
- Shows we're building for real, not learning
- Attracts enterprise users who care about long-term stability

**v0.1-v1.0 Timeline**:
```
v0.1 (now):      Rule engine foundation
v0.2 (2 weeks):  Metadata and priorities
v0.3 (3 weeks):  Oracle rules pack
v0.4 (4-6 weeks): SQL parser + AST
v0.5 (4-6 weeks): Procedures + multi-target generation
v0.6 (2-3 weeks): Validation + confidence scoring
v0.7 (3-4 weeks): Reporting + CLI
v0.8-0.9 (7-8 weeks): Additional databases (MySQL, MSSQL)
v1.0 (2 months):  Stable release, production-ready
```

**Total: 12-14 months to first stable release**

**What This Signals**:
- Not rushing (no v1.0 in 2 weeks)
- Serious about quality and testing
- Enterprise-grade timeline (other frameworks took 18-24 months)
- Realistic effort estimates

**Status**: ✅ Accepted (v0.1+)

---

## ADR-012: Why Documentation is Part of v0.1?

**Decision**: Complete documentation foundation (ARCHITECTURE, DECISIONS, ROADMAP, AI_CONTEXT) as part of v0.1, not a v0.8 afterthought.

**Rationale**:
- Documentation informs design (we design while writing ARCHITECTURE)
- Decisions are clearer when recorded (why we chose regex rules)
- Roadmap prevents feature creep (clear what's in scope)
- New contributors need context to understand codebase
- Open-source projects without good docs don't attract contributors

**What's Included**:
- ✅ ARCHITECTURE.md (system design, components, data flow)
- ✅ DECISIONS.md (why we designed it this way - this file)
- ✅ DEVELOPMENT_LOG.md (progress tracking, milestone status)
- ✅ AI_CONTEXT.md (development philosophy, conventions)
- ✅ README.md (updated with enterprise context)

**Why v0.1?**
- Foundation should be rock solid, not chaotic
- Early documentation prevents wrong assumptions
- Sets standard for future contributions

**Status**: ✅ Accepted (v0.1)

---

## Summary

These 12 ADRs form the foundation of sql-rule-engine:

| ADR | Focus | Status |
|-----|-------|--------|
| 001-003 | Core technology choices | ✅ Accepted |
| 004-007 | v0.1 engine design | ✅ Accepted |
| 008-012 | Enterprise context & roadmap | ✅ Accepted |

Together, they establish a **stable, extensible, well-documented foundation** for a production-grade SQL migration framework.
