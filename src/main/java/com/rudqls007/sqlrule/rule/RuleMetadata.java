package com.rudqls007.sqlrule.rule;

import java.util.Collections;
import java.util.Set;

/**
 * Immutable metadata container for SQL transformation rules.
 *
 * This record stores metadata extracted from the @Rule annotation on rule implementations.
 * It provides information about:
 * - Rule identity (name, version)
 * - Organization (category, priority)
 * - Scope (target databases)
 * - Description
 *
 * As a Java 21 record, instances are immutable and thread-safe.
 * All getters return immutable or defensive copies.
 */
public record RuleMetadata(
        String name,
        String version,
        RuleCategory category,
        int priority,
        Set<DatabaseTarget> targetDatabases,
        String description
) {

    /**
     * Compact constructor with validation.
     */
    public RuleMetadata {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version cannot be null or blank");
        }
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
        if (priority < 0) {
            throw new IllegalArgumentException("priority must be >= 0");
        }
        if (targetDatabases == null || targetDatabases.isEmpty()) {
            throw new IllegalArgumentException("targetDatabases cannot be null or empty");
        }
        if (description == null) {
            throw new IllegalArgumentException("description cannot be null");
        }

        // Make targetDatabases immutable
        targetDatabases = Collections.unmodifiableSet(targetDatabases);
    }

    /**
     * Returns an immutable view of target databases.
     *
     * @return unmodifiable set of target databases
     */
    @Override
    public Set<DatabaseTarget> targetDatabases() {
        return targetDatabases;
    }
}
