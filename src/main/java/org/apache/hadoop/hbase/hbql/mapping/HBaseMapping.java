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

package org.apache.hadoop.hbase.hbql.mapping;

import org.apache.expreval.expr.ExpressionTree;
import org.apache.expreval.util.Lists;
import org.apache.expreval.util.Maps;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HMapping;
import org.apache.hadoop.hbase.hbql.client.HRecord;
import org.apache.hadoop.hbase.hbql.filter.HBqlFilter;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;
import org.apache.hadoop.hbase.hbql.impl.HRecordImpl;
import org.apache.hadoop.hbase.hbql.io.IO;
import org.apache.hadoop.hbase.hbql.parser.ParserUtil;
import org.apache.hadoop.hbase.hbql.statement.MappingContext;
import org.apache.hadoop.hbase.hbql.statement.NoStatementMappingContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HBaseMapping extends Mapping implements HMapping {

    private transient HConnectionImpl connection;
    private boolean isTemp;
    private Set<String> familyNameSet = null;

    private final Map<String, ColumnAttrib> columnAttribByFamilyQualifiedNameMap = Maps.newHashMap();
    private final Map<String, ColumnAttrib> versionAttribMap = Maps.newHashMap();
    private final Map<String, ColumnAttrib> familyDefaultMap = Maps.newHashMap();
    private final Map<String, List<ColumnAttrib>> columnAttribListByFamilyNameMap = Maps.newHashMap();

    // For serialization
    public HBaseMapping() {
    }

    public HBaseMapping(final HConnectionImpl connection,
                        final boolean isTemp,
                        final String mappingName,
                        final String tableName,
                        final String keyName,
                        final List<FamilyMapping> familyMappingList) throws HBqlException {
        super(mappingName, tableName);
        this.connection = connection;
        this.isTemp = isTemp;

        // Add KEY column
        if (keyName != null)
            processColumn(ColumnDefinition.newKeyColumn(keyName));

        if (familyMappingList != null) {
            // Add columns
            for (final FamilyMapping familyDefinition : familyMappingList)
                if (familyDefinition.getColumnList() != null)
                    for (final ColumnDefinition columnDefinition : familyDefinition.getColumnList())
                        processColumn(columnDefinition);

            // Add Family Defaults
            for (final FamilyMapping familyMapping : familyMappingList) {
                if (familyMapping.includeUnmapped()) {
                    final String familyName = familyMapping.getFamilyName();
                    final ColumnDefinition columnDefinition = ColumnDefinition.newFamilyDefaultColumn(familyName);
                    final HRecordAttrib attrib = new HRecordAttrib(columnDefinition);
                    this.addFamilyDefaultAttrib(attrib);
                }
            }
        }
    }

    private HConnectionImpl getConnection() {
        return this.connection;
    }

    public HRecord newHRecord() throws HBqlException {
        final MappingContext mappingContext = new NoStatementMappingContext(this, null);
        mappingContext.setResultMapping(new HRecordResultMapping(mappingContext));
        return new HRecordImpl(mappingContext);
    }

    private void processColumn(final ColumnDefinition columnDefinition) throws HBqlException {

        final HRecordAttrib attrib = new HRecordAttrib(columnDefinition);

        this.addAttribToVariableNameMap(attrib, attrib.getNamesForColumn());
        this.addAttribToFamilyQualifiedNameMap(attrib);
        this.addVersionAttrib(attrib);
        this.addAttribToFamilyNameColumnListMap(attrib);

        if (attrib.isAKeyAttrib()) {
            if (this.getKeyAttrib() != null)
                throw new HBqlException("Mapping " + this + " has multiple instance variables marked as keys");
            this.setKeyAttrib(attrib);
        }
    }

    public byte[] getTableNameAsBytes() throws HBqlException {
        return IO.getSerialization().getStringAsBytes(this.getTableName());
    }

    // *** columnAttribByFamilyQualifiedNameMap calls
    protected Map<String, ColumnAttrib> getAttribByFamilyQualifiedNameMap() {
        return this.columnAttribByFamilyQualifiedNameMap;
    }

    public ColumnAttrib getAttribFromFamilyQualifiedName(final String familyName, final String columnName) {
        return this.getAttribFromFamilyQualifiedName(familyName + ":" + columnName);
    }

    public ColumnAttrib getAttribFromFamilyQualifiedName(final String familyQualifiedName) {
        return this.getAttribByFamilyQualifiedNameMap().get(familyQualifiedName);
    }

    protected void addAttribToFamilyQualifiedNameMap(final ColumnAttrib attrib) throws HBqlException {

        if (attrib.isFamilyDefaultAttrib())
            return;

        final String name = attrib.getFamilyQualifiedName();
        if (this.getAttribByFamilyQualifiedNameMap().containsKey(name))
            throw new HBqlException(name + " already declared");
        this.getAttribByFamilyQualifiedNameMap().put(name, attrib);
    }

    // *** familyDefaultMap calls
    private Map<String, ColumnAttrib> getFamilyDefaultMap() {
        return this.familyDefaultMap;
    }

    public ColumnAttrib getFamilyDefault(final String name) {
        return this.getFamilyDefaultMap().get(name);
    }

    private void addFamilyDefaultAttrib(final ColumnAttrib attrib) throws HBqlException {

        final String familyName = attrib.getFamilyName();
        if (this.getFamilyDefaultMap().containsKey(familyName))
            throw new HBqlException(familyName + " already declared");

        this.getFamilyDefaultMap().put(familyName, attrib);

        final String aliasName = attrib.getAliasName();
        if (aliasName == null || aliasName.length() == 0 || aliasName.equals(familyName))
            return;

        if (this.getFamilyDefaultMap().containsKey(aliasName))
            throw new HBqlException(aliasName + " already declared");

        this.getFamilyDefaultMap().put(aliasName, attrib);
    }

    // *** versionAttribByFamilyQualifiedNameMap calls
    private Map<String, ColumnAttrib> getVersionAttribMap() {
        return this.versionAttribMap;
    }

    public ColumnAttrib getVersionAttrib(final String name) {
        return this.getVersionAttribMap().get(name);
    }

    public ColumnAttrib getVersionAttribMap(final String familyName, final String columnName) {
        return this.getVersionAttrib(familyName + ":" + columnName);
    }

    protected void addVersionAttrib(final ColumnAttrib attrib) throws HBqlException {

        if (!attrib.isAVersionValue())
            return;

        final String familyQualifiedName = attrib.getFamilyQualifiedName();
        if (this.getVersionAttribMap().containsKey(familyQualifiedName))
            throw new HBqlException(familyQualifiedName + " already declared");

        this.getVersionAttribMap().put(familyQualifiedName, attrib);
    }

    // *** columnAttribListByFamilyNameMap
    private Map<String, List<ColumnAttrib>> getColumnAttribListByFamilyNameMap() {
        return this.columnAttribListByFamilyNameMap;
    }

    public Set<String> getFamilySet() {
        return this.getColumnAttribListByFamilyNameMap().keySet();
    }

    public List<ColumnAttrib> getColumnAttribListByFamilyName(final String familyName) {
        return this.getColumnAttribListByFamilyNameMap().get(familyName);
    }

    public boolean containsFamilyNameInFamilyNameMap(final String familyName) {
        return this.getColumnAttribListByFamilyNameMap().containsKey(familyName);
    }

    public void addAttribToFamilyNameColumnListMap(final String familyName,
                                                   final List<ColumnAttrib> attribList) throws HBqlException {
        if (this.containsFamilyNameInFamilyNameMap(familyName))
            throw new HBqlException(familyName + " already declared");
        this.getColumnAttribListByFamilyNameMap().put(familyName, attribList);
    }

    public void addAttribToFamilyNameColumnListMap(ColumnAttrib attrib) throws HBqlException {

        if (attrib.isAKeyAttrib() || attrib.isFamilyDefaultAttrib())
            return;

        final String familyName = attrib.getFamilyName();

        if (familyName == null || familyName.length() == 0)
            return;

        final List<ColumnAttrib> attribList;
        if (!this.containsFamilyNameInFamilyNameMap(familyName)) {
            attribList = Lists.newArrayList();
            this.addAttribToFamilyNameColumnListMap(familyName, attribList);
        }
        else {
            attribList = this.getColumnAttribListByFamilyName(familyName);
        }
        attribList.add(attrib);
    }

    public synchronized Set<String> getMappingFamilyNames() throws HBqlException {

        // TODO May not want to cache this
        if (this.familyNameSet == null) {
            // Connction will be null from tests
            this.familyNameSet = (this.getConnection() == null)
                                 ? this.getFamilySet()
                                 : this.getConnection().getFamilyNames(this.getTableName());
        }

        return this.familyNameSet;
    }

    public HBqlFilter newHBqlFilter(final String query) throws HBqlException {
        final MappingContext mappingContext = new NoStatementMappingContext(this, null);
        mappingContext.setResultMapping(new HRecordResultMapping(mappingContext));
        final ExpressionTree expressionTree = ParserUtil.parseWhereExpression(query, mappingContext);
        return new HBqlFilter(expressionTree);
    }

    public boolean isTempMapping() {
        return this.isTemp;
    }

    public void dropMapping() throws HBqlException {
        this.getConnection().dropMapping(this.getMappingName());
    }
}