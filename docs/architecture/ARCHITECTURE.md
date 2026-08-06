# SQL Rule Engine - Architecture

## 1. System Overview

**v0.1 Mission**: Build a solid foundation for rule-based SQL transformation that can scale to enterprise migrations.

The current version (v0.1) focuses on the core rule engine. Future phases will add sophisticated features like dependency analysis, confidence scoring, and multi-database code generation.

```
Current (v0.1):
[Input SQL] → [Rule Engine] → [Output SQL]

Future (v0.5+):
[Input] → [Parse] → [Analyze] → [Transform] → [Validate] → [Report] → [Generate] → [Output]
```

### Core Principles

1. **Extensibility** — New rules can be added without modifying engine code
2. **Single Responsibility** — Each component has one job
3. **Testability** — Small, focused components are easy to test
4. **Maintainability** — Clear abstractions and documentation
5. **Enterprise Scale** — Designed to handle 100s of objects eventually

---

## 2. v0.1 Components

### 2.1 SqlRule Interface

**Location**: `src/main/java/com/rudqls007/sqlrule/rule/SqlRule.java`

**Purpose**: Define contract for SQL transformation rules

**Contract**:
```java
public interface SqlRule {
    boolean supports(String sql);    // Does this rule apply?
    String convert(String sql);      // Apply the transformation
}
```

**Why This Design?**
- Loose coupling: Each rule is independent
- Open-Closed Principle: Add rules without changing engine
- Simple contract makes rules easy to implement and test
- Future: Can add metadata (priority, confidence, target-DB) in v0.2

**Current Implementations**:
- `NvlToCoalesceRule` — Oracle NVL() → COALESCE()

**Known Limitations** (intentional for v0.1):
- Regex-based only (no parsing)
- Cannot handle nested parentheses reliably
- Will be replaced with AST-based transformations in v0.4

---

### 2.2 RuleEngine

**Location**: `src/main/java/com/rudqls007/sqlrule/engine/RuleEngine.java`

**Purpose**: Apply registered rules to SQL text

**How It Works**:
```
1. Receive SQL string
2. For each registered rule (in order):
   a. Check if rule.supports(sql) returns true
   b. If yes, sql = rule.convert(sql)
   c. Pass result to next rule
3. Return final transformed SQL
```

**Design Rationale**:
- Sequential, stateless pipeline
- Simple to understand and test
- No conflicts: first-match wins
- Future: v0.3+ will add priorities and phases

**Example Flow**:
```
Input: "SELECT NVL(name, 'N/A') FROM users"
       ↓
RuleEngine checks:
  - NvlToCoalesceRule.supports() → true
  - NvlToCoalesceRule.convert() → "SELECT COALESCE(name, 'N/A') FROM users"
  - [Next rule...]
       ↓
Output: "SELECT COALESCE(name, 'N/A') FROM users"
```

---

### 2.3 RuleRegistry

**Location**: `src/main/java/com/rudqls007/sqlrule/engine/RuleRegistry.java`

**Purpose**: Store and provide registered rules to engine

**Interface**:
```java
public class RuleRegistry {
    public void register(SqlRule rule)           // Add a rule
    public List<SqlRule> getRules()              // Get all rules (unmodifiable)
}
```

**Design Rationale**:
- Centralized rule management
- Decouples rule storage from execution
- Unmodifiable list prevents accidental modification
- Throws NullPointerException on null rule (fail-fast principle)

**Future Enhancements** (v0.2+):
- Rule metadata (priority, category, confidence level)
- Rule enable/disable
- Load rules from configuration files or plugins

---

### 2.4 Specific Rule Implementations

**NvlToCoalesceRule**:
- **Purpose**: Convert Oracle NVL() to standard COALESCE()
- **Implementation**: Regex-based pattern matching
- **Limitations**: Cannot handle nested parentheses (intentional)
- **Examples**:
  - ✓ Works: `NVL(col, 'N/A')` → `COALESCE(col, 'N/A')`
  - ✗ Fails: `NVL(SUBSTR(col, 1, 3), 'N/A')` (nested function)

---

## 3. Package Structure

```
src/main/java/com/rudqls007/sqlrule/

├── rule/                           # Rule abstractions and implementations
│   ├── SqlRule.java                # Core rule interface (v0.1)
│   └── impl/
│       └── NvlToCoalesceRule.java  # First rule implementation (v0.1)
│
├── engine/                         # Orchestration
│   ├── RuleEngine.java             # Transformation orchestrator (v0.1)
│   └── RuleRegistry.java           # Rule storage (v0.1)
│
├── exception/                      # Custom exceptions (reserved for v0.2+)
├── converter/                      # AST transformation (reserved for v0.5+)
├── parser/                         # SQL parsing (reserved for v0.4+)
├── generator/                      # Code generation (reserved for v0.5+)
├── validator/                      # Validation (reserved for v0.6+)
│
└── SqlRuleApplication.java         # Entry point demo (v0.1)
```

**Rationale for This Structure**:
- Organized by responsibility, not by database or feature
- Clear home for new rules (`rule/impl/`)
- Ready for future components without restructuring
- Scales to 100+ rules naturally

---

## 4. Data Flow (v0.1)

```
┌─────────────────────────────────────────────┐
│      1. Create RuleRegistry                 │
│      registry = new RuleRegistry()          │
└─────────────────┬───────────────────────────┘
                  ↓
┌─────────────────────────────────────────────┐
│      2. Register Rules                      │
│      registry.register(                     │
│        new NvlToCoalesceRule())             │
└─────────────────┬───────────────────────────┘
                  ↓
┌─────────────────────────────────────────────┐
│      3. Create Engine                       │
│      engine = new RuleEngine(registry)      │
└─────────────────┬───────────────────────────┘
                  ↓
┌─────────────────────────────────────────────┐
│      4. Convert SQL                         │
│      input = "SELECT NVL(a, b) FROM t"     │
│      output = engine.convert(input)         │
└─────────────────┬───────────────────────────┘
                  ↓
┌─────────────────────────────────────────────┐
│      5. Result                              │
│      output = "SELECT COALESCE(a, b)...     │
│                FROM t"                      │
└─────────────────────────────────────────────┘
```

---

## 5. Extension Points (Open-Closed Principle)

### Adding a New Rule (No Engine Changes)

```java
// 1. Implement SqlRule
public class MyNewRule implements SqlRule {
    public boolean supports(String sql) { ... }
    public String convert(String sql) { ... }
}

// 2. Register it
registry.register(new MyNewRule());

// That's it. Engine automatically applies it.
```

### Adding Rule Metadata (Future - v0.2+)

```java
@Rule(
    name = "NVL to COALESCE",
    category = "FUNCTION_CONVERSION",
    priority = 100,
    confidence = CONFIDENCE.HIGH
)
public class NvlToCoalesceRule implements SqlRule { ... }
```

### Adding Multiple Target Databases (Future - v0.5+)

Each target database will have its own generator:
```java
PostgreSqlGenerator gen = new PostgreSqlGenerator();
String pgSql = gen.generate(ast);

MysqlGenerator gen2 = new MysqlGenerator();
String mySql = gen2.generate(ast);
```

---

## 6. Design Decisions in v0.1

| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| Regex-based rules | Quick to implement, test real patterns | Fails on nested syntax |
| Sequential pipeline | Simple, no conflicts | No priorities yet |
| No parsing | Focus on engine foundation | Limited to simple expressions |
| Simple registry | Good enough for <50 rules | Will need enhancement at scale |

---

## 7. Known Limitations (Intentional)

| Limitation | Why | Solution |
|-----------|-----|----------|
| Regex-based conversions | Learn what patterns exist before building parser | v0.4: Proper SQL parser |
| Single-pass pipeline | Simple to test and understand | v0.3: Add priority system |
| No confidence scoring | Not needed yet | v0.6: Validate conversions |
| No multi-target support | Focus on PostgreSQL first | v0.5: Add generators per target |
| No procedural logic | Need parser first | v0.5: Add PL/SQL support |

---

## 8. Why This Architecture Scales to Enterprise

### Rule Engine as Foundation
- New rules slide in without touching core code
- 100+ rules can coexist peacefully
- Each rule is tested independently
- No cascading dependencies

### Registry Pattern
- Rules are decoupled from engine
- Can load from files, databases, plugins (v0.2+)
- Enable/disable rules without code change
- Version rules independently

### Single Responsibility
- RuleRegistry: Store rules
- RuleEngine: Apply rules
- SqlRule: Define transformation
- Each component has one job, can be tested/modified independently

### Ready for Future Growth
- Parser slots in v0.4 without breaking engine
- Confidence scoring adds to SqlRule in v0.2
- Multi-target generators in v0.5
- Batch processing in v0.7

---

## 9. Future Architecture Evolution

### v0.2: Rule Metadata & Priority
```
Current: RuleEngine loops through rules in order
Future: RuleEngine respects priority, phase, target-DB
```

### v0.3: Simple Phases
```
Phase 1: Critical fixes (highest priority)
Phase 2: Standard conversions
Phase 3: Optional enhancements
```

### v0.4: Add Parser & AST
```
Current: Regex rules operate on text
Future: Parser → AST → Transformer (using rules) → Generator
```

### v0.5: Multi-Target Generation
```
Same AST → Different generators → Different target databases
```

### v0.6: Validation & Confidence
```
Each transformation tracked: HIGH/MEDIUM/LOW confidence
Confidence score summarized in migration report
```

### v0.7+: Batch Processing & CLI
```
Migrate entire projects, not just individual SQL statements
```

---

## 10. Technology Rationale

**Why Java 21?**
- Enterprise standard for large-scale systems
- Strong parsing ecosystem
- Type safety catches conversion errors
- Excellent IDE tooling
- LTS (Long-Term Support)

**Why Gradle?**
- Flexible build system
- Strong dependency management
- Maven-compatible
- Extensible for future build steps

**Why JUnit 5?**
- Modern testing framework
- Supports parameterized tests (useful for rule testing)
- Easy to write comprehensive tests

---

## 11. Testing Strategy (v0.1)

### Unit Tests
- One test class per production class
- Test public API contract (supports, convert)
- Include positive, negative, edge cases
- Descriptive test names

**Example**:
```java
class NvlToCoalesceRuleTest {
    void convertsSimpleNvl() { }           // positive case
    void leavesUnmatchedSqlUnchanged() { } // negative case
    void handlesCaseInsensitivity() { }    // edge case
}
```

### Integration Tests
- End-to-end RuleEngine with multiple rules
- Verify output is valid SQL for target database

---

## 12. Code Quality Standards

- **Naming**: PascalCase for classes (SqlRule, RuleEngine), camelCase for methods
- **Comments**: Explain "why", not "what"
- **Javadoc**: Public classes and methods
- **Testing**: Every rule has comprehensive tests
- **Git**: One logical change per commit

---

## Summary

v0.1 establishes a **solid, extensible foundation** for rule-based SQL transformation:

✅ **Scalable Design**: Rules are independent, registry decouples storage
✅ **Clear Abstraction**: SqlRule interface is simple but powerful
✅ **Testable**: Small components, easy to test in isolation
✅ **Documented**: Architecture is clear, decisions are explained
✅ **Ready for Growth**: Future components plug in without refactoring

The next phases will add sophistication, but the foundation remains stable.
