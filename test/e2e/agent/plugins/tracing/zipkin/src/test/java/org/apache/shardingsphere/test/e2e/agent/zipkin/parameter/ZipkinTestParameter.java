package org.apache.shardingsphere.test.e2e.agent.zipkin.parameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

@RequiredArgsConstructor
@Getter
@ToString
public final class ZipkinTestParameter {
    
    private final String scenario = "zipkin";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final String adaptorType = "proxy";
}
