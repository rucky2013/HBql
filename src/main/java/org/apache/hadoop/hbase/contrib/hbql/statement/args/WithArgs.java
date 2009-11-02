package org.apache.hadoop.hbase.contrib.hbql.statement.args;

import org.apache.expreval.client.HBqlException;
import org.apache.expreval.expr.ExpressionTree;
import org.apache.expreval.util.Lists;
import org.apache.expreval.util.Sets;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.contrib.hbql.filter.HBqlFilter;
import org.apache.hadoop.hbase.contrib.hbql.schema.ColumnAttrib;
import org.apache.hadoop.hbase.contrib.hbql.schema.HBaseSchema;
import org.apache.hadoop.hbase.contrib.hbql.statement.select.RowRequest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class WithArgs {

    private KeyRangeArgs keyRangeArgs = null;
    private TimestampArgs timestampArgs = null;
    private VersionArgs versionArgs = null;
    private LimitArgs limitArgs = null;
    private ExpressionTree clientExpressionTree = null;
    private ExpressionTree serverExpressionTree = null;

    private HBaseSchema schema;

    // Keep track of args set multiple times
    private final Set<String> multipleSetValues = Sets.newHashSet();

    public void setSchema(final HBaseSchema schema) throws HBqlException {

        this.schema = schema;

        this.validateWithArgs();

        if (this.getKeyRangeArgs() == null)
            this.setKeyRangeArgs(new KeyRangeArgs());    // Defualt to ALL records

        this.getKeyRangeArgs().setSchema(null);

        if (this.getTimestampArgs() != null)
            this.getTimestampArgs().setSchema(null);

        if (this.getVersionArgs() != null)
            this.getVersionArgs().setSchema(null);

        if (this.getLimitArgs() != null)
            this.getLimitArgs().setSchema(null);

        if (this.getServerExpressionTree() != null)
            this.getServerExpressionTree().setSchema(this.getSchema());

        if (this.getClientExpressionTree() != null)
            this.getClientExpressionTree().setSchema(this.getSchema());
    }

    private void validateWithArgs() throws HBqlException {
        if (this.multipleSetValues.size() > 0) {
            final StringBuilder sbuf = new StringBuilder();
            boolean firstTime = true;
            for (final String str : this.multipleSetValues) {
                if (!firstTime)
                    sbuf.append(", ");
                sbuf.append(str);
                firstTime = false;
            }
            throw new HBqlException("Select args specificed multiple times: " + sbuf);
        }
    }

    private HBaseSchema getSchema() {
        return this.schema;
    }

    private void addError(final String str) {
        this.multipleSetValues.add(str);
    }

    private KeyRangeArgs getKeyRangeArgs() {
        return this.keyRangeArgs;
    }

    public void setKeyRangeArgs(final KeyRangeArgs keyRangeArgs) {
        if (this.getKeyRangeArgs() != null)
            this.addError("Keys");
        this.keyRangeArgs = keyRangeArgs;
    }

    private TimestampArgs getTimestampArgs() {
        return this.timestampArgs;
    }

    public void setTimestampArgs(final TimestampArgs timestampArgs) {
        if (this.getTimestampArgs() != null)
            this.addError("Time Range");
        this.timestampArgs = timestampArgs;
    }

    private VersionArgs getVersionArgs() {
        return this.versionArgs;
    }

    public void setVersionArgs(final VersionArgs versionArgs) {
        if (this.getVersionArgs() != null)
            this.addError("Version");
        this.versionArgs = versionArgs;
    }

    public LimitArgs getLimitArgs() {
        return this.limitArgs;
    }

    public void setLimitArgs(final LimitArgs limitArgs) {
        if (this.getLimitArgs() != null)
            this.addError("Limit");
        this.limitArgs = limitArgs;
    }

    public ExpressionTree getClientExpressionTree() {
        return this.clientExpressionTree;
    }

    public void setClientExpressionTree(final ExpressionTree clientExpressionTree) {
        if (this.getClientExpressionTree() != null)
            this.addError("Client Where");
        this.clientExpressionTree = clientExpressionTree;
    }

    public ExpressionTree getServerExpressionTree() {
        return serverExpressionTree;
    }

    public void setServerExpressionTree(final ExpressionTree serverExpressionTree) {
        if (this.getServerExpressionTree() != null)
            this.addError("Server Where");
        this.serverExpressionTree = serverExpressionTree;
    }

    public long getLimit() throws HBqlException {
        return (this.getLimitArgs() != null) ? this.getLimitArgs().getValue() : 0;
    }

    public String asString() {

        final StringBuilder sbuf = new StringBuilder("WITH ");

        if (this.getKeyRangeArgs() != null)
            sbuf.append(this.getKeyRangeArgs().asString() + "\n");

        if (this.getTimestampArgs() != null)
            sbuf.append(this.getTimestampArgs().asString() + "\n");

        if (this.getVersionArgs() != null)
            sbuf.append(this.getVersionArgs().asString() + "\n");

        if (this.getLimitArgs() != null)
            sbuf.append(this.getLimitArgs().asString() + "\n");

        if (this.getServerExpressionTree() != null)
            sbuf.append("SERVER FILTER " + this.getServerExpressionTree().asString() + "\n");

        if (this.getClientExpressionTree() != null)
            sbuf.append("CLIENT FILTER " + this.getClientExpressionTree().asString() + "\n");

        return sbuf.toString();
    }

    public int setParameter(final String name, final Object val) throws HBqlException {

        int cnt = this.getKeyRangeArgs().setParameter(name, val);

        if (this.getTimestampArgs() != null)
            cnt += this.getTimestampArgs().setParameter(name, val);

        if (this.getVersionArgs() != null)
            cnt += this.getVersionArgs().setParameter(name, val);

        if (this.getLimitArgs() != null)
            cnt += this.getLimitArgs().setParameter(name, val);

        if (this.getServerExpressionTree() != null)
            cnt += this.getServerExpressionTree().setParameter(name, val);

        if (this.getClientExpressionTree() != null)
            cnt += this.getClientExpressionTree().setParameter(name, val);

        return cnt;
    }

    public Set<ColumnAttrib> getAllColumnsUsedInExprs() {
        final Set<ColumnAttrib> allAttribs = Sets.newHashSet();
        if (this.getServerExpressionTree() != null)
            allAttribs.addAll(this.getServerExpressionTree().getAttribsUsedInExpr());
        if (this.getClientExpressionTree() != null)
            allAttribs.addAll(this.getClientExpressionTree().getAttribsUsedInExpr());
        return allAttribs;
    }

    public List<RowRequest> getRowRequestList(final Collection<ColumnAttrib> columnAttribSet)
            throws IOException, HBqlException {

        final List<RowRequest> rowRequestList = Lists.newArrayList();
        for (final KeyRangeArgs.Range range : this.getKeyRangeArgs().getRangeList())
            range.process(this, rowRequestList, columnAttribSet);
        return rowRequestList;
    }

    public void setGetArgs(final Get get,
                           final Collection<ColumnAttrib> columnAttribSet) throws HBqlException, IOException {

        // Set column names
        for (final ColumnAttrib attrib : columnAttribSet) {

            // Do not bother to request because it will always be delivered
            if (attrib.isAKeyAttrib())
                continue;

            // If it is a map, then request all columns for family
            if (attrib.isASelectFamilyAttrib() || attrib.isMapKeysAsColumnsAttrib())
                get.addFamily(attrib.getFamilyNameAsBytes());
            else
                get.addColumn(attrib.getFamilyNameAsBytes(), attrib.getColumnNameAsBytes());
        }

        if (this.getTimestampArgs() != null)
            this.getTimestampArgs().setTimeStamp(get);

        if (this.getVersionArgs() != null)
            this.getVersionArgs().setMaxVersions(get);

        final HBqlFilter serverFilter = this.getSchema().getHBqlFilter(this.getServerExpressionTree(),
                                                                       this.getLimit());
        if (serverFilter != null)
            get.setFilter(serverFilter);
    }

    public void setScanArgs(final Scan scan,
                            final Collection<ColumnAttrib> columnAttribSet) throws HBqlException, IOException {

        // Set column names
        for (final ColumnAttrib attrib : columnAttribSet) {

            // Do not bother to request because it will always be delivered
            if (attrib.isAKeyAttrib())
                continue;

            // If it is a map, then request all columns for family
            if (attrib.isASelectFamilyAttrib() || attrib.isMapKeysAsColumnsAttrib())
                scan.addFamily(attrib.getFamilyNameAsBytes());
            else
                scan.addColumn(attrib.getFamilyNameAsBytes(), attrib.getColumnNameAsBytes());
        }

        if (this.getTimestampArgs() != null)
            this.getTimestampArgs().setTimeStamp(scan);

        if (this.getVersionArgs() != null)
            this.getVersionArgs().setMaxVersions(scan);

        final HBqlFilter serverFilter = this.getSchema().getHBqlFilter(this.getServerExpressionTree(),
                                                                       this.getLimit());
        if (serverFilter != null)
            scan.setFilter(serverFilter);
    }
}
