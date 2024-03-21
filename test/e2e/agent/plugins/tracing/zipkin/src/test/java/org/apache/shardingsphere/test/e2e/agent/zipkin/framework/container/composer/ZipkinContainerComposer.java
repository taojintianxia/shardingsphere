package org.apache.shardingsphere.test.e2e.agent.zipkin.framework.container.composer;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.test.e2e.agent.zipkin.parameter.ZipkinTestParameter;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.ITContainers;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereProxyStandaloneContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.plugin.tracing.TracingContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.plugin.tracing.TracingContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ZipkinContainerComposer implements AutoCloseable {
    
    @Getter
    private final ITContainers containers;
    
    @Getter
    private final DockerITContainer proxyITContainer;
    
    public ZipkinContainerComposer(ZipkinTestParameter parameter) {
        containers = new ITContainers(parameter.getScenario());
        TracingContainer tracingContainer = containers.registerContainer(TracingContainerFactory.newInstance("Zipkin"));
        tracingContainer.setNetworkAliases(Collections.singletonList("agent.plugin.zipkin.host"));
        DockerStorageContainer storageContainer = containers.registerContainer((DockerStorageContainer) StorageContainerFactory.newInstance(new MySQLDatabaseType(), "mysql:5.7",
                StorageContainerConfigurationFactory.newInstance(new MySQLDatabaseType(), 5)));
        storageContainer.setNetworkAliases(Collections.singletonList("mysql.agent.proxy.zipkin.host"));
        AdaptorContainerConfiguration containerConfig = new AdaptorContainerConfiguration(parameter.getScenario(),
                getMountedResources(parameter.getAdaptorType()), "apache/shardingsphere-proxy-agent-tracing-zipkin-test", "");
        DockerITContainer proxyContainer = new ShardingSphereProxyStandaloneContainer(parameter.getDatabaseType(), containerConfig);
        containers.registerContainer(proxyContainer);
        proxyContainer.dependsOn(tracingContainer, storageContainer);
        proxyITContainer = proxyContainer;
    }
    
    private Map<String, String> getMountedResources(final String adaptorType) {
        Map<String, String> result = new HashMap<>(1, 1F);
        result.put(String.format("/docker/%s/conf", "proxy"), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
        result.put("/docker/agent/conf", "/opt/shardingsphere-proxy/agent/conf");
        return result;
    }
    
    public void start() {
        containers.start();
    }
    
    @Override
    public void close() throws Exception {
        containers.stop();
    }
}
