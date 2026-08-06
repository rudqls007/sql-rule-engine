package com.rudqls007.sqlrule.rule;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RuleMetadataTest {

    @Test
    void testRuleMetadataCreation() {
        Set<DatabaseTarget> targets = Set.of(DatabaseTarget.POSTGRESQL, DatabaseTarget.MYSQL);
        RuleMetadata metadata = new RuleMetadata(
            "Test Rule",
            "1.0",
            RuleCategory.FUNCTION_CONVERSION,
            100,
            targets,
            "A test rule"
        );

        assertEquals("Test Rule", metadata.name());
        assertEquals("1.0", metadata.version());
        assertEquals(RuleCategory.FUNCTION_CONVERSION, metadata.category());
        assertEquals(100, metadata.priority());
        assertEquals(targets, metadata.targetDatabases());
        assertEquals("A test rule", metadata.description());
    }

    @Test
    void testRuleMetadataImmutability() {
        Set<DatabaseTarget> targets = Set.of(DatabaseTarget.POSTGRESQL);
        RuleMetadata metadata = new RuleMetadata(
            "Test Rule",
            "1.0",
            RuleCategory.GENERAL,
            50,
            targets,
            ""
        );

        Set<DatabaseTarget> retrieved = metadata.targetDatabases();
        assertThrows(UnsupportedOperationException.class, () -> retrieved.add(DatabaseTarget.MYSQL));
    }

    @Test
    void testRuleMetadataValidation_NullName() {
        assertThrows(IllegalArgumentException.class, () -> new RuleMetadata(
            null,
            "1.0",
            RuleCategory.GENERAL,
            100,
            Set.of(DatabaseTarget.POSTGRESQL),
            ""
        ));
    }

    @Test
    void testRuleMetadataValidation_BlankName() {
        assertThrows(IllegalArgumentException.class, () -> new RuleMetadata(
            "   ",
            "1.0",
            RuleCategory.GENERAL,
            100,
            Set.of(DatabaseTarget.POSTGRESQL),
            ""
        ));
    }

    @Test
    void testRuleMetadataValidation_NullVersion() {
        assertThrows(IllegalArgumentException.class, () -> new RuleMetadata(
            "Test",
            null,
            RuleCategory.GENERAL,
            100,
            Set.of(DatabaseTarget.POSTGRESQL),
            ""
        ));
    }

    @Test
    void testRuleMetadataValidation_NullCategory() {
        assertThrows(IllegalArgumentException.class, () -> new RuleMetadata(
            "Test",
            "1.0",
            null,
            100,
            Set.of(DatabaseTarget.POSTGRESQL),
            ""
        ));
    }

    @Test
    void testRuleMetadataValidation_NegativePriority() {
        assertThrows(IllegalArgumentException.class, () -> new RuleMetadata(
            "Test",
            "1.0",
            RuleCategory.GENERAL,
            -1,
            Set.of(DatabaseTarget.POSTGRESQL),
            ""
        ));
    }

    @Test
    void testRuleMetadataValidation_EmptyTargets() {
        assertThrows(IllegalArgumentException.class, () -> new RuleMetadata(
            "Test",
            "1.0",
            RuleCategory.GENERAL,
            100,
            Set.of(),
            ""
        ));
    }

    @Test
    void testRuleMetadataValidation_NullTargets() {
        assertThrows(IllegalArgumentException.class, () -> new RuleMetadata(
            "Test",
            "1.0",
            RuleCategory.GENERAL,
            100,
            null,
            ""
        ));
    }

    @Test
    void testRuleMetadataValidation_NullDescription() {
        assertThrows(IllegalArgumentException.class, () -> new RuleMetadata(
            "Test",
            "1.0",
            RuleCategory.GENERAL,
            100,
            Set.of(DatabaseTarget.POSTGRESQL),
            null
        ));
    }

    @Test
    void testRuleMetadataWithZeroPriority() {
        RuleMetadata metadata = new RuleMetadata(
            "Test",
            "1.0",
            RuleCategory.GENERAL,
            0,
            Set.of(DatabaseTarget.POSTGRESQL),
            ""
        );
        assertEquals(0, metadata.priority());
    }

    @Test
    void testRuleMetadataWithHighPriority() {
        RuleMetadata metadata = new RuleMetadata(
            "Test",
            "1.0",
            RuleCategory.GENERAL,
            1000,
            Set.of(DatabaseTarget.POSTGRESQL),
            ""
        );
        assertEquals(1000, metadata.priority());
    }
}
