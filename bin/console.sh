#!/bin/bash

#HBQL_HOME=/Users/pambrose/git/hbql
#TRX_HOME=/Users/pambrose/git/hbase-transactional-tableindexed
#HBASE_HOME=/Users/pambrose/src/hbase-0.90.0

HBASE_CLASSPATH=$HBASE_HOME/conf:$HBASE_HOME/hbase-0.90.0.jar:$TRX_HOME/org/apache/hbase-trx-0.90.0-DEV-2.jar:$HBASE_HOME/lib/hadoop-0.20.1-hdfs127-core.jar:$HBASE_HOME/lib/zookeeper-3.2.1.jar:$HBASE_HOME/lib/log4j-1.2.15.jar:$HBASE_HOME/lib/commons-logging-1.0.4.jar
HBQL_CLASSPATH=$HBQL_HOME/lib/jline-0.9.94.jar:$HBQL_HOME/lib/antlr-runtime-3.1.3.jar
HBQL_JAR=$HBQL_HOME/hbql-0.90.0.1

java -classpath $CLASSPATH:$HBASE_CLASSPATH:$HBQL_CLASSPATH:$HBQL_JAR org.apache.hadoop.hbase.hbql.Console $*
