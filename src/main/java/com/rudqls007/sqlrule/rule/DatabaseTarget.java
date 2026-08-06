package com.rudqls007.sqlrule.rule;

/**
 * Target database types supported by the SQL Rule Engine.
 *
 * Each target represents a RDBMS system for which conversion rules can be defined.
 * Rules can specify which target databases they apply to via the @Rule annotation.
 */
public enum DatabaseTarget {
    /** PostgreSQL database */
    POSTGRESQL,

    /** MySQL database */
    MYSQL,

    /** Microsoft SQL Server */
    MSSQL,

    /** eXperDB (Oracle-compatible) */
    EXPERDB
}
