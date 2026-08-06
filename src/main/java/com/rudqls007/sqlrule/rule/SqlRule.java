package com.rudqls007.sqlrule.rule;

/**
 * Core abstraction for SQL transformation rules.
 *
 * A rule encapsulates a single, reusable transformation pattern that can be
 * applied to SQL text. Each rule implementation handles one specific conversion
 * (e.g., NVL to COALESCE, SYSDATE to CURRENT_TIMESTAMP).
 *
 * The two-method contract allows the engine to:
 * 1. Check if a rule applies (supports)
 * 2. Apply the transformation (convert)
 *
 * This design supports extensibility - new rules can be added without modifying
 * the engine, satisfying the Open-Closed Principle.
 */
public interface SqlRule {

    /**
     * Determines whether this rule can handle the given SQL text.
     *
     * @param sql the SQL text to check
     * @return true if this rule applies, false otherwise
     */
    boolean supports(String sql);

    /**
     * Applies the transformation to the given SQL text.
     *
     * This method should only be called after confirming that supports(sql) returns true.
     * However, implementations should handle edge cases gracefully (null input, etc.).
     *
     * @param sql the SQL text to transform
     * @return the transformed SQL text, or the original if transformation fails
     */
    String convert(String sql);

}
