/*
 * Copyright (c) 2009.  The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.hbql.parser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.expreval.client.InternalErrorException;
import org.apache.expreval.client.LexerRecognitionException;
import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.ExpressionTree;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.antlr.HBqlLexer;
import org.apache.hadoop.hbase.hbql.antlr.HBqlParser;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HPreparedStatement;
import org.apache.hadoop.hbase.hbql.client.ParseException;
import org.apache.hadoop.hbase.hbql.statement.ConnectionStatement;
import org.apache.hadoop.hbase.hbql.statement.HBqlStatement;
import org.apache.hadoop.hbase.hbql.statement.NonConnectionStatement;
import org.apache.hadoop.hbase.hbql.statement.SchemaContext;
import org.apache.hadoop.hbase.hbql.statement.SelectStatement;
import org.apache.hadoop.hbase.hbql.statement.args.WithArgs;
import org.apache.hadoop.hbase.hbql.statement.select.SingleExpressionContext;

import java.util.List;

public class ParserUtil {

    private static final Log log = LogFactory.getLog(ParserUtil.class.getName());

    public static HBqlParser newHBqlParser(final String sql) throws ParseException {
        try {
            log.debug("Parsing: " + sql);
            final Lexer lex = new HBqlLexer(new ANTLRStringStream(sql));
            final CommonTokenStream tokens = new CommonTokenStream(lex);
            return new HBqlParser(tokens);
        }
        catch (LexerRecognitionException e) {
            throw new ParseException(e.getRecognitionExecption(), e.getMessage());
        }
    }

    public static ExpressionTree parseWhereExpression(final String sql,
                                                      final SchemaContext schemaContext) throws HBqlException {
        try {
            return schemaContext.getSchema().getExpressionTree(sql, schemaContext);
        }
        catch (RecognitionException e) {
            e.printStackTrace();
            throw new HBqlException("Error parsing: " + sql);
        }
    }

    public static Object parseExpression(final String sql) throws HBqlException {
        try {
            final HBqlParser parser = ParserUtil.newHBqlParser(sql);
            final GenericValue valueExpr = parser.topExpr();
            valueExpr.validateTypes(null, false);
            return valueExpr.getValue(null);
        }
        catch (ResultMissingColumnException e) {
            // No column refes to be missing
            throw new InternalErrorException(e.getMessage());
        }
        catch (RecognitionException e) {
            e.printStackTrace();
            throw new ParseException(e, sql);
        }
    }

    public static SingleExpressionContext parseSelectElement(final String sql) throws HBqlException {
        try {
            final HBqlParser parser = ParserUtil.newHBqlParser(sql);
            final SingleExpressionContext elem = (SingleExpressionContext)parser.selectElem();
            elem.setSchemaContext(null);
            return elem;
        }
        catch (RecognitionException e) {
            e.printStackTrace();
            throw new ParseException(e, sql);
        }
    }

    public static Object evaluateSelectElement(final SingleExpressionContext elem) throws HBqlException {
        return elem.getValue(null);
    }

    public static WithArgs parseWithClause(final String sql) throws ParseException {
        try {
            final HBqlParser parser = ParserUtil.newHBqlParser(sql);
            return parser.withClause();
        }
        catch (RecognitionException e) {
            e.printStackTrace();
            throw new ParseException(e, sql);
        }
    }

    public static List<HBqlStatement> parseConsoleStatements(final String sql) throws ParseException {
        try {
            final HBqlParser parser = ParserUtil.newHBqlParser(sql);
            return parser.consoleStatements();
        }
        catch (LexerRecognitionException e) {
            throw new ParseException(e.getRecognitionExecption(), sql);
        }
        catch (RecognitionException e) {
            throw new ParseException(e, sql);
        }
    }

    public static HBqlStatement parseJdbcStatement(final String sql) throws ParseException {
        try {
            final HBqlParser parser = ParserUtil.newHBqlParser(sql);
            return parser.jdbcStatement();
        }
        catch (LexerRecognitionException e) {
            throw new ParseException(e.getRecognitionExecption(), sql);
        }
        catch (RecognitionException e) {
            throw new ParseException(e, sql);
        }
    }

    private static HBqlStatement parse(final String sql) throws ParseException {
        try {
            final HBqlParser parser = ParserUtil.newHBqlParser(sql);
            return parser.consoleStatement();
        }
        catch (RecognitionException e) {
            throw new ParseException(e, sql);
        }
        catch (LexerRecognitionException e) {
            throw new ParseException(e.getRecognitionExecption(), sql);
        }
    }

    public static NonConnectionStatement parseSchemaManagerStatement(final String sql) throws HBqlException {
        final HBqlStatement statement = ParserUtil.parse(sql);

        if (!(statement instanceof NonConnectionStatement))
            throw new HBqlException("Expecting a schema manager statement");

        return (NonConnectionStatement)statement;
    }

    public static ConnectionStatement parseConnectionStatement(final String sql) throws HBqlException {

        final HBqlStatement statement = ParserUtil.parse(sql);

        if (!(statement instanceof ConnectionStatement))
            throw new HBqlException("Expecting a connection statement");

        return (ConnectionStatement)statement;
    }

    public static HPreparedStatement parsePreparedStatement(final String sql) throws HBqlException {

        final HBqlStatement statement = parse(sql);

        if (!(statement instanceof HPreparedStatement))
            throw new HBqlException("Expecting a prepared statement");

        return (HPreparedStatement)statement;
    }

    public static SelectStatement parseSelectStatement(final String sql) throws HBqlException {

        final HBqlStatement statement = ParserUtil.parse(sql);

        if (!(statement instanceof SelectStatement))
            throw new HBqlException("Expecting a SELECT statement");

        return (SelectStatement)statement;
    }
}
