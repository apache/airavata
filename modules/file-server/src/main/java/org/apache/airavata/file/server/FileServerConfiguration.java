package org.apache.airavata.file.server;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class FileServerConfiguration {

    @Bean
    public AdaptorSupport adaptorSupport() {
        return AdaptorSupportImpl.getInstance();
    }

    //regserver.server.host
    @Value("${regserver.server.host:localhost}")
    private String registryServerHost;
    //regserver.server.port

    @Value("${regserver.server.port:8970}")
    private int registryServerPort;

    @Bean
    public ThriftClientPool<RegistryService.Client> registryClientPool() {
        GenericObjectPoolConfig<RegistryService.Client> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        // must set timeBetweenEvictionRunsMillis since eviction doesn't run unless that is positive
        poolConfig.setTimeBetweenEvictionRunsMillis(5L * 60L * 1000L);
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWaitMillis(3000);

        return new ThriftClientPool<>(
                RegistryService.Client::new, poolConfig, registryServerHost, registryServerPort);
    }
}
