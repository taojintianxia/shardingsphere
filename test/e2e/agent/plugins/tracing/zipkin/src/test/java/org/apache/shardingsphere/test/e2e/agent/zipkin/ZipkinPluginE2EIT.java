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

package org.apache.shardingsphere.test.e2e.agent.zipkin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.e2e.agent.common.AgentTestActionExtension;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.zipkin.asserts.SpanAssert;
import org.apache.shardingsphere.test.e2e.agent.zipkin.cases.IntegrationTestCasesLoader;
import org.apache.shardingsphere.test.e2e.agent.zipkin.cases.SpanTestCase;
import org.apache.shardingsphere.test.e2e.agent.zipkin.framework.container.composer.ZipkinContainerComposer;
import org.apache.shardingsphere.test.e2e.agent.zipkin.parameter.ZipkinTestParameter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.stream.Stream;

//@ExtendWith(AgentTestActionExtension.class)
class ZipkinPluginE2EIT {
    
    @ParameterizedTest
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertWithAgent(final SpanTestCase spanTestCase) throws Exception {
        try(ZipkinContainerComposer containerComposer = new ZipkinContainerComposer(new ZipkinTestParameter())){
            containerComposer.start();
            int port = containerComposer.getProxyITContainer().getMappedPort(3307);
            DataSource dataSource = createHikariCP(E2ETestEnvironment.getInstance().getProps(), String.format("jdbc:mysql://127.0.0.1:%d/zipkin?useSSL=false&useLocalSessionState=true&characterEncoding=utf-8", port));
            E2ETestEnvironment.getInstance().setDataSource(dataSource);
            new AgentTestActionExtension().requestProxy();
            SpanAssert.assertIs(E2ETestEnvironment.getInstance().getProps().getProperty("zipkin.url"), spanTestCase);
        }
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return IntegrationTestCasesLoader.getInstance().loadIntegrationTestCases(E2ETestEnvironment.getInstance().getAdapter()).stream().map(Arguments::of);
        }
    }
    
    private static DataSource createHikariCP(final Properties props, final String jdbcURL) {
        HikariConfig result = new HikariConfig();
        result.setDriverClassName("com.mysql.cj.jdbc.Driver");
        result.setJdbcUrl(jdbcURL);
        result.setUsername(props.getProperty("proxy.username", "proxy"));
        result.setPassword(props.getProperty("proxy.password", "Proxy@123"));
        result.setMaximumPoolSize(5);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return new HikariDataSource(result);
    }
}
