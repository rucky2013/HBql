package org.apache.hadoop.hbase.hbql.query.expr.value.var;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprContext;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.BooleanLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.DateLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.DoubleLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.FloatLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.IntegerLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.LongLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.ShortLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.StringLiteral;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.StringNullLiteral;
import org.apache.hadoop.hbase.hbql.query.util.HUtil;
import org.apache.hadoop.hbase.hbql.query.util.Lists;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class NamedParameter implements GenericValue {

    private ExprContext context = null;
    private GenericValue typedExpr = null;
    private List<GenericValue> typedExprList = null;

    private final String paramName;

    public NamedParameter(final String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return this.paramName;
    }

    private boolean isScalarValueSet() {
        return this.getTypedExpr() != null;
    }

    private GenericValue getTypedExpr() {
        return this.typedExpr;
    }

    private List<GenericValue> getTypedExprList() {
        return this.typedExprList;
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowsCollections) throws TypeException {

        if (this.getTypedExpr() == null && this.getTypedExprList() == null)
            throw new TypeException("Parameter " + this.getParamName() + " not assigned a value");

        if (this.isScalarValueSet()) {
            return this.getTypedExpr().getClass();
        }
        else {
            // Make sure a list is legal in this expr
            if (!allowsCollections)
                throw new TypeException("Parameter " + this.getParamName()
                                        + " is assigned a collection which is not allowed in the context "
                                        + parentExpr.asString());

            // if it is a list, then ensure that all the types in list are valid and consistent
            if (this.getTypedExprList().size() == 0)
                throw new TypeException("Parameter " + this.getParamName() + " is assigned a collection with no values");

            // Look at the type of the first item and then make sure the rest match that one
            final GenericValue firstval = this.getTypedExprList().get(0);
            final Class<? extends GenericValue> clazzToMatch = HUtil.getGenericExprType(firstval);

            for (final GenericValue val : this.getTypedExprList()) {

                final Class<? extends GenericValue> clazz = HUtil.getGenericExprType(val);

                if (clazz == null)
                    throw new TypeException("Parameter " + this.getParamName()
                                            + " assigned a collection value with invalid type "
                                            + firstval.getClass().getSimpleName());

                if (!clazz.equals(clazzToMatch))
                    throw new TypeException("Parameter " + this.getParamName()
                                            + " assigned a collection value with type "
                                            + firstval.getClass().getSimpleName()
                                            + " which is inconsistent with the type of the first element");
            }

            return clazzToMatch;
        }
    }

    public Object getValue(final Object object) throws HBqlException {
        if (this.isScalarValueSet())
            return this.getTypedExpr().getValue(object);
        else
            return this.getTypedExprList();
    }

    public void setExprContext(final ExprContext context) throws HBqlException {
        this.context = context;
        this.context.addNamedParameter(this);
    }

    public ExprContext getContext() {
        return this.context;
    }

    public GenericValue getOptimizedValue() throws HBqlException {
        return this;
    }

    public boolean isAConstant() {
        return false;
    }

    public void setParameter(final Object val) throws HBqlException {

        // Reset both values
        this.typedExpr = null;
        this.typedExprList = null;

        if (val != null && HUtil.isACollection(val)) {
            this.typedExprList = Lists.newArrayList();
            for (final Object elem : (Collection)val)
                this.typedExprList.add(this.getValueExpr(elem));
        }
        else {
            this.typedExpr = this.getValueExpr(val);
        }
    }

    private GenericValue getValueExpr(final Object val) throws TypeException {

        if (val == null)
            return new StringNullLiteral();

        if (val instanceof Boolean)
            return new BooleanLiteral((Boolean)val);

        if (val instanceof String)
            return new StringLiteral((String)val);

        if (val instanceof Date)
            return new DateLiteral((Date)val);

        if (val instanceof Short)
            return new ShortLiteral((Short)val);

        if (val instanceof Integer)
            return new IntegerLiteral((Integer)val);

        if (val instanceof Long)
            return new LongLiteral((Long)val);

        if (val instanceof Float)
            return new FloatLiteral((Float)val);

        if (val instanceof Double)
            return new DoubleLiteral((Double)val);

        throw new TypeException("Parameter " + this.getParamName()
                                + " assigned an unsupported type " + val.getClass().getSimpleName());
    }

    public String asString() {
        return this.getParamName();
    }
}