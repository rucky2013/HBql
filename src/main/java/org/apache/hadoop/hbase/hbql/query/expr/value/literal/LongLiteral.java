package org.apache.hadoop.hbase.hbql.query.expr.value.literal;

import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.LongValue;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class LongLiteral extends GenericLiteral implements LongValue {

    private final Long value;

    public LongLiteral(final Long value) {
        this.value = value;
    }

    @Override
    public Long getValue(final Object object) {
        return this.value;
    }

    @Override
    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowsCollections) throws TypeException {
        return LongValue.class;
    }

    @Override
    public String asString() {
        return "" + this.value;
    }
}