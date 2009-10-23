package org.apache.hadoop.hbase.hbql.query.cmds.table;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HOutput;
import org.apache.hadoop.hbase.hbql.query.cmds.ConnectionCommand;
import org.apache.hadoop.hbase.hbql.query.impl.hbase.ConnectionImpl;

import java.io.IOException;

public class DropTable extends TableCommand implements ConnectionCommand {

    public DropTable(final String tableName) {
        super(tableName);
    }


    public HOutput execute(final ConnectionImpl conn) throws HBqlException, IOException {

        conn.getAdmin().deleteTable(this.getTableName());

        final HOutput retval = new HOutput();
        retval.out.println("Table " + this.getTableName() + " dropped.");
        retval.out.flush();
        return retval;
    }
}