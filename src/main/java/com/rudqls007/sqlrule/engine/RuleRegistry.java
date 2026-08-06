package com.rudqls007.sqlrule.engine;

import com.rudqls007.sqlrule.rule.DatabaseTarget;
import com.rudqls007.sqlrule.rule.Rule;
import com.rudqls007.sqlrule.rule.RuleCategory;
import com.rudqls007.sqlrule.rule.RuleMetadata;
import com.rudqls007.sqlrule.rule.SqlRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In-memory registry for SqlRule implementations with metadata support.
 *
 * This registry:
 * 1. Holds registered rules with their extracted metadata
 * 2. Extracts metadata from @Rule annotations via reflection at registration time
 * 3. Provides rules sorted by priority (highest first) while preserving registration order
 *    for rules with the same priority (stable sort)
 * 4. Maintains backward compatibility - rules without @Rule annotation get default metadata
 *
 * Priority-based execution allows fine-grained control over rule application order
 * without modifying the SqlRule interface.
 */
public class RuleRegistry {

    private final List<SqlRule> rules = new ArrayList<>();
    private final Map<SqlRule, RuleMetadata> metadataMap = new HashMap<>();

    /**
     * Registers a rule with this registry.
     *
     * Extracts metadata from the @Rule annotation if present, or uses default metadata
     * if the annotation is missing. This maintains backward compatibility with rules
     * that don't declare the @Rule annotation.
     *
     * @param rule the rule to register
     * @throws NullPointerException if rule is null
     */
    public void register(SqlRule rule) {
        if (rule == null) throw new NullPointerException("rule");
        
        this.rules.add(rule);
        this.metadataMap.put(rule, extractMetadata(rule));
    }

    /**
     * Returns an unmodifiable list of registered rules in registration order.
     *
     * This method is provided for backward compatibility. For priority-based
     * execution, use getRulesByPriority() instead.
     *
     * @return the registered rules in registration order
     */
    public List<SqlRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Returns an unmodifiable list of registered rules sorted by priority (high to low).
     *
     * Sorting is stable: rules with the same priority maintain their registration order.
     * This method is used by RuleEngine for priority-based rule execution.
     *
     * @return rules sorted by priority (descending), with same-priority rules in registration order
     */
    public List<SqlRule> getRulesByPriority() {
        return rules.stream()
            .sorted((r1, r2) -> {
                RuleMetadata m1 = metadataMap.get(r1);
                RuleMetadata m2 = metadataMap.get(r2);
                // Compare priority (higher first)
                int priorityCompare = Integer.compare(m2.priority(), m1.priority());
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                // Same priority: preserve registration order
                return Integer.compare(rules.indexOf(r1), rules.indexOf(r2));
            })
            .toList();
    }

    /**
     * Returns the metadata for a registered rule.
     *
     * @param rule the rule to retrieve metadata for
     * @return metadata for the rule, or null if rule is not registered
     */
    public RuleMetadata getMetadata(SqlRule rule) {
        return metadataMap.get(rule);
    }

    /**
     * Extracts metadata from a rule's @Rule annotation or creates default metadata.
     *
     * If the rule class is annotated with @Rule, its metadata is extracted.
     * Otherwise, a default RuleMetadata with generic values is created.
     *
     * @param rule the rule to extract metadata from
     * @return metadata extracted from the annotation or default metadata
     */
    private RuleMetadata extractMetadata(SqlRule rule) {
        Rule annotation = rule.getClass().getAnnotation(Rule.class);
        
        if (annotation == null) {
            // Default metadata for backward compatibility
            return new RuleMetadata(
                rule.getClass().getSimpleName(),
                "1.0",
                RuleCategory.GENERAL,
                100,
                Set.of(DatabaseTarget.POSTGRESQL),
                ""
            );
        }
        
        // Extract metadata from annotation
        return new RuleMetadata(
            annotation.name(),
            annotation.version(),
            annotation.category(),
            annotation.priority(),
            Set.of(annotation.targetDatabases()),
            annotation.description()
        );
    }
}
