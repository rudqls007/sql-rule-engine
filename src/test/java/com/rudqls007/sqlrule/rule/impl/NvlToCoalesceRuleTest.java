package com.rudqls007.sqlrule.rule.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NvlToCoalesceRuleTest {

    @Test
    void convertsSimpleNvl() {
        var rule = new NvlToCoalesceRule();
        var input = "SELECT NVL(a, b) FROM t";
        assertTrue(rule.supports(input));
        assertEquals("SELECT COALESCE(a, b) FROM t", rule.convert(input));
    }

    @Test
    void leavesNonMatching() {
        var rule = new NvlToCoalesceRule();
        var input = "SELECT a FROM t";
        assertFalse(rule.supports(input));
        assertEquals(input, rule.convert(input));
    }
}
