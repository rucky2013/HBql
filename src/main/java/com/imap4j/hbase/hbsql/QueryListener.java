package com.imap4j.hbase.hbsql;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 20, 2009
 * Time: 10:38:45 PM
 */
public interface QueryListener<T extends Persistable> {

    void onQueryInit();

    void onEachRow(T val) throws PersistException;

    void onQueryCompletion();

}