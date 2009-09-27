package org.apache.hadoop.hbase.hbql.query.antlr.args;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.NumberValue;
import org.apache.hadoop.hbase.hbql.query.schema.HUtil;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 4, 2009
 * Time: 10:26:18 AM
 */
public class VersionArgs {

    private final GenericValue value;

    public VersionArgs(final GenericValue val) {
        this.value = val;
    }

    public boolean isValid() {
        return this.value != null;
    }

    public int getValue() throws HBqlException {

        if (this.value == null)
            throw new HBqlException("Null value invalid");

        final Class clazz = this.value.getClass();
        if (!HUtil.isParentClass(NumberValue.class, clazz))
            throw new HBqlException("Invalid type " + clazz.getSimpleName());

        return ((Number)this.value.getValue(null)).intValue();
    }
}