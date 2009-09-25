package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.StringValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.BooleanLiteral;
import org.apache.hadoop.hbase.hbql.query.schema.HUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class LikeStmt extends GenericNotValue {

    private ValueExpr valueExpr = null;
    private ValueExpr patternExpr = null;

    private Pattern pattern = null;

    public LikeStmt(final ValueExpr valueExpr, final boolean not, final ValueExpr patternExpr) {
        super(not);
        this.valueExpr = valueExpr;
        this.patternExpr = patternExpr;
    }

    private ValueExpr getValueExpr() {
        return this.valueExpr;
    }

    private ValueExpr getPatternExpr() {
        return this.patternExpr;
    }

    private Pattern getPattern() {
        return this.pattern;
    }

    @Override
    public Boolean getValue(final Object object) throws HBqlException {

        if (this.getPatternExpr().isAConstant()) {
            if (this.pattern == null) {
                final String pattern = (String)this.getPatternExpr().getValue(object);
                this.pattern = Pattern.compile(pattern);
            }
        }
        else {
            final String pattern = (String)this.getPatternExpr().getValue(object);
            if (pattern == null)
                throw new HBqlException("Null string for LIKE pattern");
            this.pattern = Pattern.compile(pattern);
        }

        final String val = (String)this.getValueExpr().getValue(object);
        if (val == null)
            throw new HBqlException("Null string for LIKE value");

        final Matcher m = this.getPattern().matcher(val);

        final boolean retval = m.matches();

        return (this.isNot()) ? !retval : retval;
    }

    @Override
    public Class<? extends ValueExpr> validateType() throws HBqlException {

        final Class<? extends ValueExpr> value = this.getValueExpr().validateType();
        final Class<? extends ValueExpr> pattern = this.getPatternExpr().validateType();

        if (!HUtil.isParentClass(StringValue.class, value, pattern))
            throw new HBqlException("Invalid types "
                                    + value.getName() + " " + pattern.getName() + " in LikeStmt");

        return BooleanValue.class;
    }

    @Override
    public ValueExpr getOptimizedValue() throws HBqlException {
        this.valueExpr = this.getValueExpr().getOptimizedValue();
        this.patternExpr = this.getPatternExpr().getOptimizedValue();

        return this.isAConstant() ? new BooleanLiteral(this.getValue(null)) : this;
    }

    @Override
    public boolean isAConstant() {
        return this.getValueExpr().isAConstant() && this.getPatternExpr().isAConstant();
    }

    @Override
    public void setContext(final ExprTree context) {
        this.getValueExpr().setContext(context);
        this.getPatternExpr().setContext(context);
    }

    @Override
    public String asString() {
        return this.getValueExpr().asString() + notAsString() + " LIKE "
               + this.getPatternExpr().asString();
    }

}