/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.binder.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.dml.MergeStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementBinder;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;

import java.util.List;

/**
 * SQL bind engine.
 */
@RequiredArgsConstructor
public final class SQLBindEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final String currentDatabaseName;
    
    private final HintValueContext hintValueContext;
    
    /**
     * Bind SQL statement with metadata.
     *
     * @param sqlStatement SQL statement
     * @param params parameters
     * @return SQL statement context
     */
    public SQLStatementContext bind(final SQLStatement sqlStatement, final List<Object> params) {
        SQLStatement buoundedSQLStatement = bind(sqlStatement, metaData, currentDatabaseName);
        return SQLStatementContextFactory.newInstance(metaData, params, buoundedSQLStatement, currentDatabaseName);
    }
    
    private SQLStatement bind(final SQLStatement statement, final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        if (hintValueContext.findHintDataSourceName().isPresent()) {
            return statement;
        }
        if (statement instanceof DMLStatement) {
            return bindDMLStatement(statement, metaData, currentDatabaseName);
        }
        if (statement instanceof DDLStatement) {
            return bindDDLStatement(statement, metaData, currentDatabaseName);
        }
        return statement;
    }
    
    private static SQLStatement bindDMLStatement(final SQLStatement statement, final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        if (statement instanceof SelectStatement) {
            return new SelectStatementBinder().bind((SelectStatement) statement, metaData, currentDatabaseName);
        }
        if (statement instanceof InsertStatement) {
            return new InsertStatementBinder().bind((InsertStatement) statement, metaData, currentDatabaseName);
        }
        if (statement instanceof UpdateStatement) {
            return new UpdateStatementBinder().bind((UpdateStatement) statement, metaData, currentDatabaseName);
        }
        if (statement instanceof DeleteStatement) {
            return new DeleteStatementBinder().bind((DeleteStatement) statement, metaData, currentDatabaseName);
        }
        if (statement instanceof MergeStatement) {
            return new MergeStatementBinder().bind((MergeStatement) statement, metaData, currentDatabaseName);
        }
        return statement;
    }
    
    private static SQLStatement bindDDLStatement(final SQLStatement statement, final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        if (statement instanceof CursorStatement) {
            return new CursorStatementBinder().bind((CursorStatement) statement, metaData, currentDatabaseName);
        }
        return statement;
    }
}
