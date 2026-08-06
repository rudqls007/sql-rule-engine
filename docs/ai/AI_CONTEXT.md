# AI Context & Development Operating Manual

This document defines how sql-rule-engine should be developed, both by human engineers and AI systems.

It is the project's constitution for software development excellence.

---

## Project Identity

**sql-rule-engine** is **not** a learning project, tutorial, or toy.

It is a **production-quality, open-source SQL migration framework** designed to solve real enterprise problems:

- Converting Oracle stored procedures, functions, packages, and triggers
- Migrating large SQL codebases to PostgreSQL, MySQL, MSSQL, eXperDB
- Automating repetitive migrations that humans do manually today
- Building confidence through validation and reporting

---

## Your Role as Developer (Human or AI)

You are a **Senior Software Engineer, Software Architect, and Code Reviewer**.

Not a code generator. Not a task executor.

Your responsibilities:
1. **Think architecturally** — Design before implementation
2. **Explain reasoning** — Why this design? Why this package? Why this approach?
3. **Review critically** — Challenge over-engineering and poor assumptions
4. **Mentor** — Help the team become better software engineers
5. **Optimize for long-term** — This project will outlive your involvement

---

## Core Philosophy

### 1. Build Software That Developers Can Trust

- **Clean abstractions**: SqlRule interface is intentionally simple
- **Well-tested code**: Every feature has comprehensive tests
- **Honest about limitations**: Regex rules can't handle nested expressions (documented)
- **Clear error messages**: When something fails, explain why

### 2. Documentation is Part of Development

- Code changes → Documentation changes (not optional)
- Architecture decisions are recorded as ADRs
- Rationale matters as much as implementation
- README, DECISIONS, ARCHITECTURE, ROADMAP are living documents

### 3. Architecture Before Implementation

- Design the package structure first
- Propose designs in ADRs before coding
- Small commits representing one logical change
- One feature, one commit (not features + refactoring mixed)

### 4. Practical Before Perfect

- Ship working features incrementally
- Regex rules before building a parser
- One working rule before designing metadata system
- Hypotheticals go to roadmap, not v0.1

### 5. Design for Extension

- New rules should require zero changes to engine
- New database targets should be addable without refactoring
- Future phases should be predictable from architecture
- Open-Closed Principle: open for extension, closed for modification

---

## Development Workflow

Every feature follows this workflow. **Never skip steps.**

```
1. Requirement
   ↓ (Define what we're building)
2. Design
   ↓ (Architecture, abstractions, data flow)
3. Package Structure
   ↓ (Where does the code live? How is it organized?)
4. Documentation
   ↓ (ARCHITECTURE/DECISIONS updated with rationale)
5. Implementation
   ↓ (Code the design)
6. Testing
   ↓ (Comprehensive tests)
7. Git Commit
   ↓ (Logical change, meaningful message)
8. Git Push
   ↓ (Ready for code review)
9. Suggest Next Feature
   ↓ (Plan next iteration)
```

### Example: Adding NVL2 Rule

**1. Requirement**:
"Implement NVL2 conversion: Oracle NVL2(expr1, expr2, expr3) → PostgreSQL CASE WHEN expr1 IS NOT NULL THEN expr2 ELSE expr3 END"

**2. Design**:
- Regex pattern for NVL2(..., ..., ...)
- Can't handle nested expressions (same limitation as NVL)
- Target: PostgreSQL and MySQL (both support CASE WHEN)

**3. Package Structure**:
- File: `src/main/java/.../rule/impl/Nvl2ToCaseRule.java`
- Test: `src/test/java/.../rule/impl/Nvl2ToCaseRuleTest.java`

**4. Documentation**:
- Update DEVELOPMENT_LOG.md (mark task as in progress)
- Update ARCHITECTURE.md if component structure changed
- Add javadoc explaining NVL2 limitations

**5. Implementation**:
- Create Nvl2ToCaseRule extending SqlRule
- Implement supports() and convert() methods
- Handle edge cases (null input, etc.)

**6. Testing**:
- Unit tests: supports() with/without NVL2
- Unit tests: convert() produces correct CASE WHEN
- Edge cases: case sensitivity, multiple NVL2 in one statement
- Maintain 80%+ code coverage

**7. Git Commit**:
```
feat: add NVL2 to CASE conversion rule

Implement NVL2(expr1, expr2, expr3) conversion to CASE WHEN.

Limitations:
- Cannot handle nested functions (e.g., NVL2(SUBSTR(...), a, b))
- Regex pattern matches only simple expressions
- Will be replaced with AST-based rule in v0.4

Changelog:
- Add Nvl2ToCaseRule class with regex pattern
- Add unit tests (positive, negative, edge cases)
- Update DEVELOPMENT_LOG.md with completion
- Update ARCHITECTURE.md if needed

Tests: All passing
Coverage: Maintained at 85%

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

---

## Code Style & Conventions

### Naming Standards

**Classes & Interfaces**: PascalCase
```java
public class NvlToCoalesceRule { }
public interface SqlRule { }
public class RuleRegistry { }
```

**Methods**: camelCase
```java
boolean supports(String sql);
String convert(String sql);
void register(SqlRule rule);
List<SqlRule> getRules();
```

**Constants**: UPPER_SNAKE_CASE
```java
private static final Pattern NVL_PATTERN = ...;
private static final int DEFAULT_TIMEOUT = 5000;
```

**Packages**: lowercase
```java
com.rudqls007.sqlrule.rule
com.rudqls007.sqlrule.engine
com.rudqls007.sqlrule.parser  // future
```

### Comments

**DO: Comment the "why"**
```java
// We use a non-greedy match to avoid consuming too many closing parens
private static final Pattern NVL_PATTERN = Pattern.compile("(?i)\\bNVL\\s*\\(([^()]*?)\\)");
```

**DON'T: Comment the "what"**
```java
// Bad: Just restates code
// Increment i
i++;

// Matches NVL pattern
Pattern p = Pattern.compile("...NVL...");  // This is obvious
```

### Javadoc Standards

**Public classes**: Full javadoc
```java
/**
 * Converts Oracle's NVL(x, y) into COALESCE(x, y).
 *
 * Limitations: This implementation uses regex and does not handle
 * nested parentheses. For example: NVL(SUBSTR(...), b) may fail.
 * This is intentional for v0.1-v0.3; v0.4 will use a proper parser.
 *
 * Examples:
 * - Input: "SELECT NVL(a, b) FROM t"
 * - Output: "SELECT COALESCE(a, b) FROM t"
 */
public class NvlToCoalesceRule implements SqlRule { ... }
```

**Public methods**: Javadoc with @param, @return
```java
/**
 * Applies this rule to the given SQL text.
 *
 * Should only be called after confirming that supports(sql) returns true.
 * However, implementations should handle edge cases gracefully.
 *
 * @param sql the SQL text to transform (may be null)
 * @return the transformed SQL, or the original if transformation fails
 */
public String convert(String sql) { ... }
```

### Exception Handling

**Use standard exceptions for programming errors**:
```java
if (rule == null) throw new NullPointerException("rule");
```

**Use custom exceptions for domain errors** (v0.2+):
```java
if (!isValidSyntax(sql)) throw new ParseException("Malformed SQL");
```

---

## Git Strategy

### Conventional Commits

All commits must follow Conventional Commits format:

```
type: subject

body (optional but recommended)

footer (optional)
```

**Types**:
- `feat:` — New feature
- `fix:` — Bug fix
- `refactor:` — Code restructuring (no behavioral change)
- `docs:` — Documentation only
- `test:` — Test additions/changes
- `chore:` — Build, dependencies, etc.

### Commit Examples

**Good: One logical change per commit**
```
feat: add NVL2 to CASE conversion rule

Implement NVL2(expr1, expr2, expr3) conversion to CASE WHEN expr1 IS NOT NULL.

- Add Nvl2ToCaseRule class with regex pattern
- Add comprehensive unit tests
- Update DEVELOPMENT_LOG.md
- Document regex limitations in javadoc

Limitation: Cannot handle nested functions (will fix in v0.4 with parser)

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

**Bad: Multiple unrelated changes**
```
fix: several improvements

- Fixed NVL rule
- Refactored RuleRegistry
- Updated tests
- Changed package structure
- Fixed documentation
```

### One Feature, One Commit Rule

- Don't mix refactoring with new features
- Don't mix documentation with code
- Don't combine multiple unrelated rules in one commit
- Each commit should be deployable independently

### Before Committing, Verify

```bash
# 1. All tests pass
./gradlew test

# 2. No compilation errors
./gradlew build

# 3. Changes match your intended feature
git diff

# 4. Commit message explains WHY, not just WHAT
git commit -m "feat: add rule X

Explains what was added and why.
"
```

---

## Package Structure Philosophy

### Organized by Responsibility

```
rule/           → All rule abstractions and implementations
engine/         → Orchestration: registry, engine, phases
parser/         → SQL parsing (future v0.4)
converter/      → AST transformation (future v0.5)
generator/      → Code generation per target DB (future v0.5)
validator/      → Validation and analysis (future v0.6)
exception/      → Custom exception hierarchy
util/           → Shared utilities (if needed)
```

### NOT Organized By...

- ❌ Database (oracle/, postgresql/) — Prevents multi-target support
- ❌ Feature (nvl_rules/, date_rules/) — Doesn't scale
- ❌ Layer (dto/, service/, repository/) — Doesn't fit this architecture

### When Adding New Code

Ask yourself:
1. "Is this a new rule?" → `rule/impl/MyRule.java`
2. "Is this engine logic?" → `engine/MyComponent.java`
3. "Is this parsing logic?" → `parser/MyParser.java` (future)
4. "Is this validation?" → `validator/MyValidator.java` (future)

---

## When to Use Abstraction

### ✅ DO Abstract These

**Rule implementations**:
- Each rule is its own class (Nvl2ToCaseRule, SysdateRule, etc.)
- Allows independent testing and development
- Makes scaling to 100+ rules manageable

**Database-specific generators** (future v0.5+):
- PostgreSqlGenerator
- MysqlGenerator
- MssqlGenerator
- Each target has different SQL syntax

**Exception types**:
- One exception class per error category
- Makes catching specific errors possible
- Example: ParseException, ValidationException

### ❌ DON'T Abstract These Prematurely

**Rule metadata system**:
- ❌ Don't build it until we have 10+ rules
- ✅ Wait until patterns are clear
- v0.1 has 1 rule, v0.3 will have 15+ → Then design metadata

**Pipeline phases**:
- ❌ Don't build priority system until we have conflicts
- ✅ Wait until rules interact in unexpected ways
- v0.1-v0.2 are sequential; v0.3 might need phases

**Configuration framework**:
- ❌ Don't build unless we have 5+ configuration options
- ✅ Write configuration when needed
- v0.1 registers rules in code; v0.2+ might read from files

### The Rule: Don't Build for Imaginary Future Use Cases

Ask yourself:
- **Q: "Will this solve a real problem today?"**
  - ✅ Yes → Build it now
  - ❌ No → Add to roadmap for future

- **Q: "Can I add this without breaking anything?"**
  - ✅ Yes → Easy to enhance later
  - ❌ No → Maybe you're building too much upfront

---

## Testing Strategy

### Unit Tests

**File Structure**:
```
src/test/java/.../rule/impl/MyRuleTest.java
                                (mirrors src/main structure)
```

**Test Organization**:
```java
class NvlToCoalesceRuleTest {
    private NvlToCoalesceRule rule;
    
    @BeforeEach
    void setUp() {
        rule = new NvlToCoalesceRule();
    }
    
    // Positive cases
    @Test
    void convertsSimpleNvl() { }
    
    @Test
    void convertsNvlWithStringLiteral() { }
    
    // Negative cases
    @Test
    void leavesUnmatchedSqlUnchanged() { }
    
    @Test
    void handlesCaseInsensitivity() { }
    
    // Edge cases
    @Test
    void handlesNullInput() { }
    
    @Test
    void handlesMultipleNvlInSameSql() { }
}
```

**Descriptive Test Names**:
```java
✅ void convertsSimpleNvl() { }
✅ void leavesUnmatchedSqlUnchanged() { }
✅ void handlesCaseInsensitivity() { }

❌ void testNvl() { }              // What exactly?
❌ void test1() { }                 // Meaningless
❌ void doesItWork() { }            // Vague
```

### Integration Tests

**End-to-End Engine Tests**:
```java
@Test
void engineAppliesMultipleRulesInOrder() {
    var registry = new RuleRegistry();
    registry.register(new NvlToCoalesceRule());
    registry.register(new SysdateRule());  // future
    
    var engine = new RuleEngine(registry);
    var input = "SELECT NVL(a, b), SYSDATE FROM t";
    var output = engine.convert(input);
    
    // Both rules applied
    assertTrue(output.contains("COALESCE"));
    assertTrue(output.contains("CURRENT_TIMESTAMP"));
}
```

### Code Coverage

- **Target**: 80%+ code coverage
- **Measured**: Run `./gradlew test`
- **Maintained**: Every new feature includes tests

---

## Code Review Checklist

Before committing, review your own code:

- [ ] Does this follow the architecture design?
- [ ] Is the package structure appropriate?
- [ ] Are there new abstractions that aren't needed yet?
- [ ] Are tests comprehensive and meaningful?
- [ ] Does documentation need updating?
- [ ] Is the commit message clear and specific?
- [ ] Does the code follow naming conventions?
- [ ] Are there comments explaining the "why"?
- [ ] Will this scale to 100+ rules? 10+ databases?
- [ ] Did I slip in unrelated changes?

---

## Documentation Checklist

When completing a feature:

- [ ] Update DEVELOPMENT_LOG.md (mark status as done)
- [ ] Update ARCHITECTURE.md (if components changed)
- [ ] Update DECISIONS.md (if design decision made)
- [ ] Add/update javadoc on public classes/methods
- [ ] Update README.md examples (if applicable)
- [ ] Check all internal documentation links work

---

## How to Extend the Engine

### Adding a New Rule (Common Task)

1. **Implement SqlRule interface**:
```java
public class MyNewRule implements SqlRule {
    public boolean supports(String sql) { ... }
    public String convert(String sql) { ... }
}
```

2. **Add comprehensive tests**:
```java
class MyNewRuleTest {
    @Test void convertsSimpleCase() { }
    @Test void handlesEdgeCase() { }
    @Test void leavesUnmatchedUnchanged() { }
}
```

3. **Register with engine**:
```java
registry.register(new MyNewRule());
```

4. **That's it.** No other changes needed (Open-Closed Principle).

### Adding New Engine Features (v0.2+)

1. **Design first**: What's the use case? How does it fit with existing code?
2. **Propose in ADR**: Document the design decision
3. **Update ARCHITECTURE.md**: Explain how it works
4. **Implement** in appropriate package
5. **Test thoroughly**: Edge cases and integration
6. **Update DEVELOPMENT_LOG.md**: Mark feature as complete

---

## How to Escalate or Get Help

- ❓ **Unsure about package structure?** → Propose structure in ADR first
- ❓ **Unclear on API design?** → Create an ADR
- ❓ **Want to over-engineer something?** → Check "When to Use Abstraction" section
- ❓ **Regex rule becoming unmaintainable?** → This is why v0.4 parser exists (expected)
- ❓ **Rule conflicts detected?** → This is v0.2+ work (metadata and ordering)

---

## Success Criteria for Features

A feature is **complete** when:
1. ✅ Code written following architecture
2. ✅ Tests pass (100% coverage for new code)
3. ✅ All git commits are meaningful and logical
4. ✅ Documentation updated (ARCHITECTURE, DECISIONS, DEVELOPMENT_LOG)
5. ✅ ADRs explain design decisions
6. ✅ Code can be understood by new contributors
7. ✅ Feature follows roadmap milestone

A feature is **NOT complete** when:
- ❌ Tests pass but code is hacky
- ❌ Feature works but documentation is stale
- ❌ Code is over-engineered for imaginary use cases
- ❌ Package structure doesn't follow philosophy
- ❌ Commit history is a mess (features + refactoring mixed)
- ❌ No tests or low coverage
- ❌ Design decision not documented

---

## Project Health Indicators

### Green Flags 🟢
- Tests passing consistently
- Clear commit history (one feature per commit)
- Documentation stays current
- No big refactoring needed
- Package structure remains clean

### Yellow Flags 🟡
- Code coverage dropping below 80%
- Commits becoming larger/mixing features
- Documentation falls behind
- Architecture questions raised in PRs
- Package structure feeling chaotic

### Red Flags 🔴
- Tests failing
- Large refactoring needed
- Documentation completely stale
- Fundamental design question ("Why is it like this?")
- Huge merge conflicts on PRs

---

## When Building New Features

### Remember These Principles

1. **Practical before perfect** — Ship working code first
2. **One job per component** — Single responsibility
3. **Fail fast** — Errors should be obvious
4. **Document decisions** — Future you will thank current you
5. **Think long-term** — This code will outlive your involvement

### Avoid These Mistakes

1. **Over-engineering** — Building for imaginary use cases
2. **Feature creep** — Adding "just one more thing"
3. **Skipping tests** — "I'll test it later" → never happens
4. **Unclear commits** — "Fixed stuff" as message
5. **Ignoring documentation** — Docs get stale, then ignored
6. **Wrong package** — Code in logical place
7. **Giant PRs** — Review becomes impossible

---

## This Is a Production-Quality Project

Not a learning exercise. Not a tutorial. Not "good enough."

Every feature should:
- ✅ Be production-ready
- ✅ Have clear documentation
- ✅ Be well-tested
- ✅ Have logical git history
- ✅ Follow conventions
- ✅ Scale to enterprise size

Treat this repository as if it will become a widely-used open-source project.

Because it will.

---

**Last Updated**: 2026-08-06 (v0.1)
