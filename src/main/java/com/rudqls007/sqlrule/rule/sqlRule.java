package com.rudqls007.sqlrule.rule;

public interface sqlRule {

    boolean supports(String sql);

    String convert(String sql);

}
