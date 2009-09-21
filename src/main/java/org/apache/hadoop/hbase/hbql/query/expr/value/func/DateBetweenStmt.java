package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.node.DateValue;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.DateLiteral;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class DateBetweenStmt extends GenericBetweenStmt<DateValue> {

    public DateBetweenStmt(final DateValue expr, final boolean not, final DateValue lower, final DateValue upper) {
        super(not, expr, lower, upper);
    }

    @Override
    public boolean optimizeForConstants(final Object object) throws HPersistException {
        boolean retval = true;

        if (this.getExpr().optimizeForConstants(object))
            this.setExpr(new DateLiteral(this.getExpr().getValue(object)));
        else
            retval = false;

        if (this.getLower().optimizeForConstants(object))
            this.setLower(new DateLiteral(this.getLower().getValue(object)));
        else
            retval = false;

        if (this.getUpper().optimizeForConstants(object))
            this.setUpper(new DateLiteral(this.getUpper().getValue(object)));
        else
            retval = false;

        return retval;
    }

    @Override
    public Boolean getValue(final Object object) throws HPersistException {

        final long dateval = this.getExpr().getValue(object);
        final boolean retval = dateval >= this.getLower().getValue(object)
                               && dateval <= this.getUpper().getValue(object);

        return (this.isNot()) ? !retval : retval;
    }
}