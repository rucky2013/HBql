package org.apache.hadoop.hbase.contrib.hbql.statement;

public abstract class TableStatement implements ConnectionStatement {

    private final String tableName;

    protected TableStatement(final String tableName) {
        this.tableName = tableName;
    }

    protected String getTableName() {
        return tableName;
    }
}
