package com.rudqls007.sqlrule;

import com.rudqls007.sqlrule.engine.RuleEngine;
import com.rudqls007.sqlrule.engine.RuleRegistry;
import com.rudqls007.sqlrule.rule.impl.NvlToCoalesceRule;

public class SqlRuleApplication {

    public static void main(String[] args) {
        // Minimal demo to show rule registration and conversion.
        var registry = new RuleRegistry();
        registry.register(new NvlToCoalesceRule());

        var engine = new RuleEngine(registry);

        String sql = "SELECT NVL(first_name, 'N/A') FROM users WHERE NVL(status, 'ACTIVE') = 'ACTIVE'";
        System.out.println("Original SQL: " + sql);
        String converted = engine.convert(sql);
        System.out.println("Converted SQL: " + converted);
    }
}
