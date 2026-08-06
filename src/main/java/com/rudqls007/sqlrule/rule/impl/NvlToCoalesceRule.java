package com.rudqls007.sqlrule.rule.impl;

import com.rudqls007.sqlrule.rule.DatabaseTarget;
import com.rudqls007.sqlrule.rule.Rule;
import com.rudqls007.sqlrule.rule.RuleCategory;
import com.rudqls007.sqlrule.rule.SqlRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Oracle's NVL(x, y) into COALESCE(x, y).
 *
 * Limitations: this implementation uses a regex-based approach and does not
 * fully parse SQL. It handles simple NVL(...) occurrences but may not
 * correctly process nested parentheses or complex expressions. This is
 * intentional for the first incremental feature - later iterations will use
 * a proper SQL parser.
 *
 * Examples:
 * - OK: "NVL(col, 'N/A')" -> "COALESCE(col, 'N/A')"
 * - OK: "NVL(a, b)" -> "COALESCE(a, b)"
 * - FAIL: "NVL(SUBSTR(...), b)" -> May fail due to nested parentheses
 */
@Rule(
    name = "NVL to COALESCE",
    version = "1.0",
    category = RuleCategory.FUNCTION_CONVERSION,
    priority = 100,
    targetDatabases = {DatabaseTarget.POSTGRESQL, DatabaseTarget.MYSQL},
    description = "Converts Oracle NVL(x, y) to standard COALESCE(x, y)"
)
public class NvlToCoalesceRule implements SqlRule {

    // Match NVL( ... ) with a simple, non-greedy capture for the inner content.
    private static final Pattern NVL_PATTERN = Pattern.compile("(?i)\\bNVL\\s*\\(([^()]*?)\\)");

    @Override
    public boolean supports(String sql) {
        if (sql == null) return false;
        return NVL_PATTERN.matcher(sql).find();
    }

    @Override
    public String convert(String sql) {
        if (sql == null) return null;
        if (!supports(sql)) return sql;

        Matcher m = NVL_PATTERN.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String inner = m.group(1);
            String replacement = "COALESCE(" + inner + ")";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
