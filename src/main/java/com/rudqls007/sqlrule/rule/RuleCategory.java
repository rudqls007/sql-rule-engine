package com.rudqls007.sqlrule.rule;

/**
 * Categories for organizing SQL transformation rules.
 *
 * Provides semantic classification of rules to help organize, discover,
 * and manage conversion rules across different domains.
 */
public enum RuleCategory {
    /** Functions like NVL, COALESCE, DECODE */
    FUNCTION_CONVERSION,

    /** Date/time functions like SYSDATE, CURRENT_TIMESTAMP */
    DATE_CONVERSION,

    /** String manipulation functions */
    STRING_CONVERSION,

    /** Numeric and math functions */
    NUMERIC_CONVERSION,

    /** Oracle-specific system functions and packages */
    SYSTEM_CONVERSION,

    /** Miscellaneous conversions */
    GENERAL
}
