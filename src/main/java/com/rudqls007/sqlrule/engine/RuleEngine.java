package com.rudqls007.sqlrule.engine;

import com.rudqls007.sqlrule.rule.SqlRule;

/**
 * Core engine that applies registered rules to input SQL.
 *
 * Current behavior: rules are applied in priority order (highest priority first).
 * When multiple rules have the same priority, registration order is preserved (stable sort).
 *
 * When a rule supports the current SQL text, its convert() output becomes the new SQL
 * that subsequent rules will see. This creates a transformation pipeline where each rule
 * can modify the SQL for the next rule.
 */
public class RuleEngine {

    private final RuleRegistry registry;

    public RuleEngine(RuleRegistry registry) {
        this.registry = registry;
    }

    /**
     * Applies registered rules to the input SQL in priority order.
     *
     * Rules are executed from highest to lowest priority. When a rule's supports()
     * method returns true, its convert() method is called, and the result becomes
     * the input for the next rule. This continues until all rules are processed.
     *
     * @param sql the SQL to transform
     * @return the transformed SQL, or the original if no rules apply
     */
    public String convert(String sql) {
        if (sql == null) return null;
        
        String result = sql;
        for (SqlRule rule : registry.getRulesByPriority()) {
            if (rule.supports(result)) {
                result = rule.convert(result);
            }
        }
        return result;
    }
}
