# SQL Rule Engine

> A lightweight rule-based SQL conversion engine built with Java.

`sql-rule-engine` is an open-source learning project inspired by real-world database migration experience.

The goal is to design a flexible rule engine that converts vendor-specific SQL into portable SQL while studying clean architecture, object-oriented design, and testing.

---

## ✨ Features

- Rule-based SQL conversion
- Extensible architecture
- Java 21
- Gradle
- JUnit 5

---

## 🛠 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Build Tool | Gradle (Groovy DSL) |
| Test | JUnit 5 |
| Version Control | Git / GitHub |
| IDE | IntelliJ IDEA |

---

## 📂 Project Structure

```
sql-rule-engine
│
├── engine
├── rule
├── parser
├── converter
└── test
```

---

## 🚧 Roadmap

### v0.0.1

- [x] Project setup
- [ ] Rule interface
- [ ] Rule engine

### v0.1.0

- [ ] NVL → COALESCE
- [ ] SYSDATE → CURRENT_TIMESTAMP
- [ ] DECODE → CASE

### Future

- SQL Parser
- CLI
- Spring Boot API
- VSCode Extension

---

## 📄 License

MIT License
