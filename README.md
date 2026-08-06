# SQL Rule Engine

> An **enterprise-grade rule-based SQL transformation framework** for automating database migrations.

`sql-rule-engine` is a production-quality open-source project built to solve a real problem: converting thousands of Oracle database objects (stored procedures, functions, packages, triggers, SQL scripts) to target databases like PostgreSQL, MySQL, and MSSQL.

This is **not** a tutorial or toy project. Every design decision prioritizes reliability, extensibility, and long-term maintainability.

---

## 🎯 The Problem We Solve

Large enterprise systems depend on Oracle with complex database logic:
- 100s of stored procedures and functions
- 10s-100s of packages
- Complex triggers and constraints
- Thousands of SQL scripts

**Today's approach** (painful):
1. Manual SQL text replacement (error-prone)
2. Extensive testing (time-consuming)
3. No confidence metrics (risky)
4. Repeated work across projects (wasteful)

**Our solution**:
```
[Oracle DB Objects] 
    ↓
[Rule Engine: Parse, Understand, Transform]
    ↓
[Confidence Score + Migration Report]
    ↓
[Target DB: PostgreSQL | MySQL | MSSQL | eXperDB]
```

---

## ✨ v0.1 Features (Foundation)

- ✅ Extensible rule engine (SqlRule interface)
- ✅ Simple rule registry (automatic rule discovery)
- ✅ First rule implemented (NVL → COALESCE)
- ✅ Comprehensive testing (80%+ code coverage)
- ✅ Production-ready architecture
- ✅ Complete documentation (ARCHITECTURE, DECISIONS, ROADMAP)

### What's Next (Roadmap)

- 🔜 v0.2: Rule metadata and priority system
- 🔜 v0.3: Oracle functions pack (15+ rules)
- 🔜 v0.4: Proper SQL parser (replace regex)
- 🔜 v0.5: Procedural logic + multi-target generation
- 🔜 v0.6: Validation & confidence scoring
- 🔜 v0.7: Migration reporting & CLI
- 🔜 v1.0: First stable release (12-14 months)

---

## 🛠 Tech Stack

| Component | Technology | Why |
|-----------|------------|-----|
| Language | Java 21 | Enterprise-grade, strong parsing ecosystem, type safety |
| Build Tool | Gradle | Flexible, Maven-compatible, extensible |
| Testing | JUnit 5 | Modern, comprehensive, parameterized support |
| Version Control | Git / GitHub | Industry standard, open-source friendly |

---

## 📦 Installation

### Prerequisites
- Java 21+
- Gradle 8.0+

### Clone & Build

```bash
git clone https://github.com/rudqls007/sql-rule-engine.git
cd sql-rule-engine
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

---

## 🚀 Quick Start

### Basic Usage (v0.1)

```java
import com.rudqls007.sqlrule.engine.RuleEngine;
import com.rudqls007.sqlrule.engine.RuleRegistry;
import com.rudqls007.sqlrule.rule.impl.NvlToCoalesceRule;

public class Main {
    public static void main(String[] args) {
        // Set up rule registry
        var registry = new RuleRegistry();
        registry.register(new NvlToCoalesceRule());

        // Create engine
        var engine = new RuleEngine(registry);

        // Convert SQL
        String oracleSql = "SELECT NVL(name, 'Unknown') FROM users";
        String convertedSql = engine.convert(oracleSql);
        
        System.out.println(convertedSql);
        // Output: SELECT COALESCE(name, 'Unknown') FROM users
    }
}
```

### Future Usage (v0.5+)

```java
// Coming in v0.5: Support for stored procedures and multiple targets
MigrationResult result = migrationEngine.analyze(oracleProc)
    .transformToTarget(DatabaseTarget.POSTGRESQL)
    .validate()
    .generateReport();

System.out.println(result.getReport());
// - Objects converted: 92/100 (92%)
// - Confidence: HIGH (95%)
// - Manual review items: 8
// - Estimated effort: 4 hours automated + 20 hours manual

String targetSql = result.getTargetSQL();
```

---

## 📚 Documentation

All project documentation is comprehensive and up-to-date:

| Document | Purpose |
|----------|---------|
| [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) | System design, components, data flow, extension points |
| [DECISIONS.md](docs/development/DECISIONS.md) | Architecture Decision Records explaining design choices |
| [DEVELOPMENT_LOG.md](docs/development/DEVELOPMENT_LOG.md) | Progress tracking, completed work, detailed roadmap (v0.1-v1.0) |
| [AI_CONTEXT.md](docs/ai/AI_CONTEXT.md) | Development philosophy, conventions, contribution guide |

---

## 🗺️ Roadmap

### v0.1 Foundation ✅ Complete
- ✅ Rule engine architecture
- ✅ SqlRule interface
- ✅ RuleRegistry and RuleEngine
- ✅ First rule: NVL → COALESCE
- ✅ Complete documentation
- ✅ Production-quality foundation

### v0.2 Metadata & Priority (2-3 weeks)
- Rule metadata system
- Priority-based rule execution
- Custom exception hierarchy
- Enhanced error messages

### v0.3 Oracle Rules Pack (3-4 weeks)
- 15+ Oracle function conversions
- NVL2, SYSDATE, DECODE, TO_CHAR, TO_DATE, SUBSTR, INSTR, TRUNC, etc.
- Real-world Oracle SQL samples
- Pattern analysis for parser design

### v0.4 SQL Parser & AST (4-6 weeks)
- Proper SQL tokenizer
- SQL parser building AST
- Replace regex rules with AST-based transformations
- Support for complex nested expressions

### v0.5 Procedures & Multi-Target (4-6 weeks)
- Support for stored procedures and triggers
- Multi-target code generation (PostgreSQL, MySQL, MSSQL)
- Procedural logic transformation (PL/SQL → target language)

### v0.6+ More Features
See [DEVELOPMENT_LOG.md](docs/development/DEVELOPMENT_LOG.md) for complete 12-14 month roadmap to v1.0.

---

## 💡 Architecture Highlights

### Why This Design?

1. **Extensible**: New rules require zero changes to engine code (Open-Closed Principle)
2. **Scalable**: Foundation designed for 100+ rules and 10+ databases
3. **Well-Tested**: Every component has comprehensive tests
4. **Well-Documented**: Architecture and decisions are explained
5. **Enterprise-Ready**: Designed for production use, not learning

### Rule Pattern

```java
// Adding a new rule is simple - just implement SqlRule
public class MyRule implements SqlRule {
    public boolean supports(String sql) { /* check if applies */ }
    public String convert(String sql) { /* perform conversion */ }
}

// Register it
registry.register(new MyRule());

// Done. Engine automatically applies it.
```

See [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) for detailed design rationale.

---

## 🧪 Testing & Quality

- **Code Coverage**: 80%+ (measured per commit)
- **Test Framework**: JUnit 5 (modern, comprehensive)
- **Automated Builds**: Gradle (reproducible builds)
- **Clear Tests**: Descriptive test names, organized by scenario

```bash
./gradlew test          # Run all tests
./gradlew build         # Build and test
```

---

## 🤝 Contributing

This project welcomes contributions from developers who share the vision of a production-grade SQL migration framework.

### Before Contributing

**Please read:**
1. [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) — Understand the design
2. [DECISIONS.md](docs/development/DECISIONS.md) — Learn why we designed it this way
3. [AI_CONTEXT.md](docs/ai/AI_CONTEXT.md) — Development philosophy and conventions

### Contribution Workflow

1. **Propose**: Open an issue or discussion with your idea
2. **Design**: Discuss architecture before coding
3. **Implement**: Follow package structure and naming conventions
4. **Test**: Comprehensive tests for all new code
5. **Document**: Update ARCHITECTURE, DECISIONS, or DEVELOPMENT_LOG
6. **Commit**: Clean, logical git history with meaningful messages
7. **Review**: Submit PR for code review

**Key Principle**: One feature, one commit. Clear commit messages. Meaningful git history.

---

## 🎓 Development Philosophy

This project follows a simple but powerful philosophy:

1. **Build software that developers can trust** — Clean abstractions, well-tested, honest about limitations
2. **Documentation is part of development** — Architecture and decisions are recorded
3. **Architecture before implementation** — Design before coding
4. **Practical before perfect** — Ship working features incrementally  
5. **Design for extension** — New rules/databases don't require refactoring

See [AI_CONTEXT.md](docs/ai/AI_CONTEXT.md) for the complete development operating manual.

---

## 📊 Project Status

**Current Phase**: v0.1 Foundation (Alpha)

- ✅ Foundation solid
- ✅ Architecture clear
- ✅ Documentation complete
- ⏳ More rules being added (v0.2+)
- ⏳ Parser in development (v0.4)

**Not yet production-ready for large migrations.** v0.1 is the foundation; v1.0 (12-14 months out) will be enterprise-grade.

---

## 🔗 Links

- **GitHub**: [rudqls007/sql-rule-engine](https://github.com/rudqls007/sql-rule-engine)
- **Issues**: [Report bugs or request features](https://github.com/rudqls007/sql-rule-engine/issues)
- **Discussions**: [Join the community](https://github.com/rudqls007/sql-rule-engine/discussions)

---

## 📝 License

MIT License — See [LICENSE](LICENSE) for details.

This project is free and open-source. Use it, modify it, contribute to it.

---

## 🚀 Vision

We're building the foundation for a widely-used open-source SQL migration framework.

Not a tutorial. Not a learning project. Not a toy.

A production-quality tool that enterprise teams can trust to automate their database migrations safely and confidently.

**If you share this vision, let's build it together.** 🔨

---

**v0.1 Released**: 2026-08-06  
**Next Milestone**: v0.2 (2-3 weeks)  
**Long-term Goal**: v1.0 stable release (12-14 months)
