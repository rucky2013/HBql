package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.schema.HUtil;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class StringInStmt extends GenericInStmt {

    public StringInStmt(final GenericValue expr, final boolean not, final List<GenericValue> valList) {
        super(not, expr, valList);
    }

    protected boolean evaluateList(final Object object) throws HBqlException {

        final String attribVal = (String)this.getExpr().getValue(object);
        for (final GenericValue obj : this.getValueExprList()) {
            // Check if the value returned is a collection
            final Object objval = obj.getValue(object);
            if (HUtil.isParentClass(Collection.class, objval.getClass())) {
                for (final GenericValue val : (Collection<GenericValue>)objval) {
                    if (attribVal.equals(val.getValue(object)))
                        return true;
                }
            }
            else {
                if (attribVal.equals(objval))
                    return true;
            }
        }
        return false;
    }
}