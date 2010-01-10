/*
 * Copyright (c) 2010.  The Apache Software Foundation
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

package org.apache.hadoop.hbase.hbql.executor;

import org.apache.hadoop.hbase.hbql.client.AsyncExecutorPoolManager;
import org.apache.hadoop.hbase.hbql.client.HBqlException;

import java.util.List;

public class AsyncExecutorPoolDefinition {

    private final String poolName;
    private final List<ExecutorPoolProperty> executorPoolPropertyList;

    private ExecutorPoolProperty minThreadCount = null;
    private ExecutorPoolProperty maxThreadCount = null;
    private ExecutorPoolProperty keepAliveSecs = null;

    public AsyncExecutorPoolDefinition(final String poolName, final List<ExecutorPoolProperty> executorPropertyList) {
        this.poolName = poolName;
        this.executorPoolPropertyList = executorPropertyList;
    }

    public String getPoolName() {
        return this.poolName;
    }

    private List<ExecutorPoolProperty> getExecutorPoolPropertyList() {
        return this.executorPoolPropertyList;
    }

    private ExecutorPoolProperty validateProperty(final ExecutorPoolProperty assignee,
                                                  final ExecutorPoolProperty value) throws HBqlException {
        if (assignee != null)
            throw new HBqlException("Multiple " + value.getPropertyType().getDescription()
                                    + " values for " + this.getPoolName() + " not allowed");
        return value;
    }

    public void validateExecutorPoolPropertyList() throws HBqlException {

        if (this.getExecutorPoolPropertyList() == null)
            return;

        for (final ExecutorPoolProperty executorPoolProperty : this.getExecutorPoolPropertyList()) {

            executorPoolProperty.validate();

            switch (executorPoolProperty.getEnumType()) {

                case MIN_THREAD_COUNT:
                    this.minThreadCount = this.validateProperty(this.minThreadCount, executorPoolProperty);
                    break;

                case MAX_THREAD_COUNT:
                    this.maxThreadCount = this.validateProperty(this.maxThreadCount, executorPoolProperty);
                    break;

                case KEEP_ALIVE_SECS:
                    this.keepAliveSecs = this.validateProperty(this.keepAliveSecs, executorPoolProperty);
                    break;
            }
        }
    }

    public int getMinThreadCount() throws HBqlException {
        if (this.minThreadCount != null)
            return this.minThreadCount.getIntegerValue();
        else
            return AsyncExecutorPoolManager.defaultMinThreadCount;
    }

    public int getMaxThreadCount() throws HBqlException {
        if (this.maxThreadCount != null)
            return this.maxThreadCount.getIntegerValue();
        else
            return AsyncExecutorPoolManager.defaultMaxThreadCount;
    }

    public long getKeepAliveSecs() throws HBqlException {
        if (this.keepAliveSecs != null)
            return this.keepAliveSecs.getIntegerValue();
        else
            return AsyncExecutorPoolManager.defaultKeepAliveSecs;
    }

    public String asString() {
        try {
            return " MIN_THREAD_COUNT : " + this.getMinThreadCount()
                   + ", MAX_THREAD_COUNT : " + this.getMaxThreadCount()
                   + ", KEEP_ALIVE_SECS : " + this.getKeepAliveSecs();
        }
        catch (HBqlException e) {
            return "Invalid expression";
        }
    }
}