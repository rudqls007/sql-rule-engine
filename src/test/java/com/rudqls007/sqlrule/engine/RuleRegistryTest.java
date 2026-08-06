package com.rudqls007.sqlrule.engine;

import com.rudqls007.sqlrule.rule.DatabaseTarget;
import com.rudqls007.sqlrule.rule.Rule;
import com.rudqls007.sqlrule.rule.RuleCategory;
import com.rudqls007.sqlrule.rule.RuleMetadata;
import com.rudqls007.sqlrule.rule.SqlRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RuleRegistryTest {

    @Test
    void registersRuleAndExtractsMetadata() {
        var registry = new RuleRegistry();
        var rule = new TestRuleWithAnnotation();
        
        registry.register(rule);
        
        RuleMetadata metadata = registry.getMetadata(rule);
        assertNotNull(metadata);
        assertEquals("Test Conversion", metadata.name());
        assertEquals("1.0", metadata.version());
        assertEquals(RuleCategory.FUNCTION_CONVERSION, metadata.category());
        assertEquals(200, metadata.priority());
        assertEquals("Test description", metadata.description());
    }

    @Test
    void providesDefaultMetadataForRuleWithoutAnnotation() {
        var registry = new RuleRegistry();
        var rule = new TestRuleWithoutAnnotation();
        
        registry.register(rule);
        
        RuleMetadata metadata = registry.getMetadata(rule);
        assertNotNull(metadata);
        assertEquals("TestRuleWithoutAnnotation", metadata.name());
        assertEquals("1.0", metadata.version());
        assertEquals(RuleCategory.GENERAL, metadata.category());
        assertEquals(100, metadata.priority());
        assertEquals(Set.of(DatabaseTarget.POSTGRESQL), metadata.targetDatabases());
    }

    @Test
    void getRulesByPrioritySortsByPriorityDescending() {
        var registry = new RuleRegistry();
        
        registry.register(new RulePriority100A());
        registry.register(new RulePriority50());
        registry.register(new RulePriority150());
        
        List<SqlRule> sorted = registry.getRulesByPriority();
        
        assertEquals(3, sorted.size());
        // Verify priority order (high to low)
        assertEquals(150, registry.getMetadata(sorted.get(0)).priority());
        assertEquals(100, registry.getMetadata(sorted.get(1)).priority());
        assertEquals(50, registry.getMetadata(sorted.get(2)).priority());
    }

    @Test
    void preservesRegistrationOrderForSamePriority() {
        var registry = new RuleRegistry();
        
        var rule1 = new RulePriority100A();
        var rule2 = new RulePriority100B();
        var rule3 = new RulePriority100C();
        
        registry.register(rule1);
        registry.register(rule2);
        registry.register(rule3);
        
        List<SqlRule> sorted = registry.getRulesByPriority();
        
        assertEquals(3, sorted.size());
        // All have priority 100, should maintain registration order
        assertSame(rule1, sorted.get(0));
        assertSame(rule2, sorted.get(1));
        assertSame(rule3, sorted.get(2));
    }

    @Test
    void stableSortMixedPriorities() {
        var registry = new RuleRegistry();
        
        // Register in specific order
        var p100a = new RulePriority100A();
        var p50 = new RulePriority50();
        var p100b = new RulePriority100B();
        var p150 = new RulePriority150();
        
        registry.register(p100a);
        registry.register(p50);
        registry.register(p100b);
        registry.register(p150);
        
        List<SqlRule> sorted = registry.getRulesByPriority();
        
        // Should be: p150 (150), p100a (100), p100b (100), p50 (50)
        assertSame(p150, sorted.get(0));
        assertSame(p100a, sorted.get(1));
        assertSame(p100b, sorted.get(2));
        assertSame(p50, sorted.get(3));
    }

    @Test
    void getMetadataReturnsNullForUnregisteredRule() {
        var registry = new RuleRegistry();
        var rule = new TestRuleWithoutAnnotation();
        
        assertNull(registry.getMetadata(rule));
    }

    @Test
    void getRulesReturnsUnmodifiableList() {
        var registry = new RuleRegistry();
        registry.register(new TestRuleWithoutAnnotation());
        
        List<SqlRule> rules = registry.getRules();
        assertThrows(UnsupportedOperationException.class, () -> rules.add(new TestRuleWithoutAnnotation()));
    }

    @Test
    void getRulesByPriorityReturnsUnmodifiableList() {
        var registry = new RuleRegistry();
        registry.register(new TestRuleWithoutAnnotation());
        
        List<SqlRule> rules = registry.getRulesByPriority();
        assertThrows(UnsupportedOperationException.class, () -> rules.add(new TestRuleWithoutAnnotation()));
    }

    @Test
    void throwsNullPointerExceptionForNullRule() {
        var registry = new RuleRegistry();
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    // Test fixtures with @Rule annotations
    @Rule(
        name = "Test Conversion",
        version = "1.0",
        category = RuleCategory.FUNCTION_CONVERSION,
        priority = 200,
        targetDatabases = {DatabaseTarget.POSTGRESQL},
        description = "Test description"
    )
    private static class TestRuleWithAnnotation implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return false;
        }

        @Override
        public String convert(String sql) {
            return sql;
        }
    }

    private static class TestRuleWithoutAnnotation implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return false;
        }

        @Override
        public String convert(String sql) {
            return sql;
        }
    }

    @Rule(name = "P100A", priority = 100)
    private static class RulePriority100A implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return false;
        }

        @Override
        public String convert(String sql) {
            return sql;
        }
    }

    @Rule(name = "P100B", priority = 100)
    private static class RulePriority100B implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return false;
        }

        @Override
        public String convert(String sql) {
            return sql;
        }
    }

    @Rule(name = "P100C", priority = 100)
    private static class RulePriority100C implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return false;
        }

        @Override
        public String convert(String sql) {
            return sql;
        }
    }

    @Rule(name = "P50", priority = 50)
    private static class RulePriority50 implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return false;
        }

        @Override
        public String convert(String sql) {
            return sql;
        }
    }

    @Rule(name = "P150", priority = 150)
    private static class RulePriority150 implements SqlRule {
        @Override
        public boolean supports(String sql) {
            return false;
        }

        @Override
        public String convert(String sql) {
            return sql;
        }
    }
}
