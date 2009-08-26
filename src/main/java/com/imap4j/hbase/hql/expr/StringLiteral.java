package com.imap4j.hbase.hql.expr;

import com.imap4j.hbase.hql.ClassSchema;
import com.imap4j.hbase.hql.HPersistable;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class StringLiteral implements StringValue {

    private final String value;

    public StringLiteral(final String value) {
        this.value = value;
    }

    @Override
    public String getValue(final ClassSchema classSchema, final HPersistable recordObj) {
        return this.value;
    }
}