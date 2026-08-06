package com.rudqls007.sqlrule.engine;

import com.rudqls007.sqlrule.rule.Rule;
import com.rudqls007.sqlrule.rule.RuleCategory;
import com.rudqls007.sqlrule.rule.SqlRule;
import com.rudqls007.sqlrule.rule.impl.NvlToCoalesceRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {

    @Test
    void appliesRegisteredRules() {
        var reg = new RuleRegistry();
        reg.register(new NvlToCoalesceRule());
        var engine = new RuleEngine(reg);
        var input = "SELECT NVL(x, y) as val FROM dual";
        var output = engine.convert(input);
        assertEquals("SELECT COALESCE(x, y) as val FROM dual", output);
    }

    @Test
    void executesPriorityOrderNotRegistrationOrder() {
        var registry = new RuleRegistry();
        var lowPriority = new TestRuleLowPriority();
        var highPriority = new TestRuleHighPriority();
        
        // Register low priority first, high priority second
        registry.register(lowPriority);
        registry.register(highPriority);
        
        var engine = new RuleEngine(registry);
        // Both rules support and modify the input
        // High priority should execute first, then low priority
        var result = engine.convert("BASE");
        
        // If high priority executes first: "BASE" -> "HIGH(BASE)" -> "HIGH(BASE)-LOW"
        // If low priority executes first: "BASE" -> "BASE-LOW" -> "HIGH(BASE-LOW)"
        assertEquals("HIGH(BASE)-LOW", result);
    }

    @Test
    void preservesRegistrationOrderForSamePriority() {
        var registry = new RuleRegistry();
        var first = new TestRuleFirstOrder();
        var second = new TestRuleSecondOrder();
        
        registry.register(first);
        registry.register(second);
        
        var engine = new RuleEngine(registry);
        // Both have same priority, so registration order should be preserved
        var result = engine.convert("START");
        
        // First (appends "-FIRST") then second (appends "-SECOND")
        assertEquals("START-FIRST-SECOND", result);
    }

    @Test
    void handlesNullInput() {
        var registry = new RuleRegistry();
        var engine = new RuleEngine(registry);
        
        assertNull(engine.convert(null));
    }

    @Test
    void returnsOriginalSqlIfNoRulesApply() {
        var registry = new RuleRegistry();
        registry.register(new NvlToCoalesceRule());
        var engine = new RuleEngine(registry);
        
        var input = "SELECT * FROM users WHERE name = 'John'";
        var output = engine.convert(input);
        
        assertEquals(input, output);
    }

    // Test fixtures
    @Rule(name = "HighPriority", priority = 200, category = RuleCategory.GENERAL)
    private static class TestRuleHighPriority implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return true;
        }

        @Override
        public String convert(String sql) {
            return "HIGH(" + sql + ")";
        }
    }

    @Rule(name = "LowPriority", priority = 50, category = RuleCategory.GENERAL)
    private static class TestRuleLowPriority implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return true;
        }

        @Override
        public String convert(String sql) {
            return sql + "-LOW";
        }
    }

    @Rule(name = "FirstOrder", priority = 100, category = RuleCategory.GENERAL)
    private static class TestRuleFirstOrder implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return true;
        }

        @Override
        public String convert(String sql) {
            return sql + "-FIRST";
        }
    }

    @Rule(name = "SecondOrder", priority = 100, category = RuleCategory.GENERAL)
    private static class TestRuleSecondOrder implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return true;
        }

        @Override
        public String convert(String sql) {
            return sql + "-SECOND";
        }
    }
}

