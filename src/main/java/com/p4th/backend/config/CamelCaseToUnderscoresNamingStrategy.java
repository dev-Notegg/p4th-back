package com.p4th.backend.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CamelCaseToUnderscoresNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return Identifier.toIdentifier(convertToSnakeCase(name.getText()), name.isQuoted());
    }
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return Identifier.toIdentifier(convertToSnakeCase(name.getText()), name.isQuoted());
    }
    private String convertToSnakeCase(String text) {
        return text.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
