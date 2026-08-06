package com.rudqls007.sqlrule.rule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative metadata annotation for SQL transformation rules.
 *
 * This annotation allows rule implementations to declare their metadata (name, category,
 * priority, target databases, etc.) at the type level. The RuleRegistry extracts this
 * metadata at runtime via reflection and stores it for later retrieval and execution.
 *
 * Retention policy is RUNTIME to support reflection-based metadata extraction.
 *
 * Example:
 * <pre>
 * {@code
 * @Rule(
 *     name = "NVL to COALESCE",
 *     version = "1.0",
 *     category = RuleCategory.FUNCTION_CONVERSION,
 *     priority = 100,
 *     targetDatabases = {DatabaseTarget.POSTGRESQL, DatabaseTarget.MYSQL},
 *     description = "Converts Oracle NVL(x, y) to standard COALESCE(x, y)"
 * )
 * public class NvlToCoalesceRule implements SqlRule {
 *     // ...
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Rule {

    /**
     * Name of the rule.
     * Must not be blank.
     *
     * @return rule name
     */
    String name();

    /**
     * Version of the rule.
     * Allows tracking rule evolution.
     *
     * @return rule version (default: "1.0")
     */
    String version() default "1.0";

    /**
     * Category of the rule for organization.
     *
     * @return rule category (default: GENERAL)
     */
    RuleCategory category() default RuleCategory.GENERAL;

    /**
     * Execution priority (higher = earlier execution).
     *
     * Rules with higher priority execute before rules with lower priority.
     * When multiple rules have the same priority, registration order is preserved.
     *
     * @return priority value (default: 100)
     */
    int priority() default 100;

    /**
     * Target databases for this rule.
     * Indicates which database systems this rule applies to.
     *
     * @return array of target databases (default: POSTGRESQL)
     */
    DatabaseTarget[] targetDatabases() default {DatabaseTarget.POSTGRESQL};

    /**
     * Human-readable description of what this rule does.
     *
     * @return rule description (default: empty string)
     */
    String description() default "";
}
