package org.apache.hadoop.hbase.hbql.query.expr.predicate;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.GenericTwoExprExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.func.Operator;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.BooleanLiteral;
import org.apache.hadoop.hbase.hbql.query.schema.HUtil;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class BooleanCompare extends GenericTwoExprExpr implements BooleanValue {

    private final Operator op;

    public BooleanCompare(final ValueExpr expr1, final Operator op, final ValueExpr expr2) {
        super(expr1, expr2);
        this.op = op;
    }

    private Operator getOp() {
        return op;
    }

    public Class<? extends ValueExpr> validateType() throws HPersistException {

        final Class<? extends ValueExpr> type1 = this.getExpr1().validateType();
        final Class<? extends ValueExpr> type2 = this.getExpr2().validateType();

        if (!HUtil.isParentClass(BooleanValue.class, type1, type2))
            throw new HPersistException("Invalid types "
                                        + type1.getName() + " " + type2.getName() + " in BooleanCompare");

        return BooleanValue.class;
    }

    @Override
    public ValueExpr getOptimizedValue() throws HPersistException {

        this.setExpr1(this.getExpr1().getOptimizedValue());
        if (this.getExpr2() != null)
            this.setExpr2(this.getExpr2().getOptimizedValue());

        return this.isAConstant() ? new BooleanLiteral(this.getValue(null)) : this;
    }

    @Override
    public Boolean getValue(final Object object) throws HPersistException {

        final boolean expr1val = (Boolean)this.getExpr1().getValue(object);

        if (this.getExpr2() == null)
            return expr1val;

        switch (this.getOp()) {
            case OR:
                return expr1val || (Boolean)this.getExpr2().getValue(object);
            case AND:
                return expr1val && (Boolean)this.getExpr2().getValue(object);

            default:
                throw new HPersistException("Error in BooleanCompare.getValue()");
        }
    }
}