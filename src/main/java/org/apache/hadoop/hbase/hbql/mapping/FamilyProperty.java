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

import org.apache.expreval.expr.ArgumentListTypeSignature;
import org.apache.expreval.expr.MultipleExpressionContext;
import org.apache.expreval.expr.node.BooleanValue;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.expreval.expr.node.IntegerValue;
import org.apache.hadoop.hbase.hbql.client.HBqlException;

public abstract class FamilyProperty extends MultipleExpressionContext {

    public static enum Type {

        MAXVERSIONS(new ArgumentListTypeSignature(IntegerValue.class), "MAX_VERSIONS"),
        COMPRESSIONTYPE(new ArgumentListTypeSignature(), "COMPRESSION_TYPE"),
        INMEMORY(new ArgumentListTypeSignature(BooleanValue.class), "IN_MEMORY"),
        BLOCKCACHE(new ArgumentListTypeSignature(BooleanValue.class), "BLOCK_CACHE"),
        BLOCKSIZE(new ArgumentListTypeSignature(IntegerValue.class), "BLOCK_SIZE"),
        MAPFILEINDEXINTERVAL(new ArgumentListTypeSignature(IntegerValue.class), "INDEX:INTERVAL"),
        TTL(new ArgumentListTypeSignature(IntegerValue.class), "TTL"),
        BLOOMFILTER(new ArgumentListTypeSignature(BooleanValue.class), "BLOOM_FILTER");

        private final ArgumentListTypeSignature typeSignature;
        private final String description;

        Type(final ArgumentListTypeSignature typeSignature, final String description) {
            this.typeSignature = typeSignature;
            this.description = description;
        }

        public ArgumentListTypeSignature getTypeSignature() {
            return this.typeSignature;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private final Type propertyType;

    protected FamilyProperty(final Type type, final GenericValue... exprs) {
        super(type.getTypeSignature(), exprs);
        this.propertyType = type;
    }

    public Type getPropertyType() {
        return this.propertyType;
    }

    public boolean useResultData() {
        return false;
    }

    public boolean allowColumns() {
        return false;
    }

    public void validate() throws HBqlException {
        this.validateTypes(this.allowColumns(), false);
    }

    public String asString() {
        return this.getPropertyType().getDescription() + ": " + this.getGenericValue(0).asString();
    }
}