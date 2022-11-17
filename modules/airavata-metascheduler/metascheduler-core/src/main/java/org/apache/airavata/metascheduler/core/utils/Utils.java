package org.apache.airavata.metascheduler.core.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * This class contains all utility methods across scheduler sub projects
 */
public class Utils {

    private static ThriftClientPool<RegistryService.Client> registryClientPool;

    /**
     * Provides registry client to access databases
     *
     * @return RegistryService.Client
     */
    public static synchronized ThriftClientPool<RegistryService.Client> getRegistryServiceClientPool() {
        if (registryClientPool != null) {
            return registryClientPool;
        }
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
            final String serverHost = ServerSettings.getRegistryServerHost();
            registryClientPool = new ThriftClientPool<>(
                    tProtocol -> new RegistryService.Client(tProtocol),
                    Utils.<RegistryService.Client>createGenericObjectPoolConfig(),
                    serverHost,
                    serverPort);
            return registryClientPool;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }

    private static <T> GenericObjectPoolConfig<T> createGenericObjectPoolConfig() {

        GenericObjectPoolConfig<T> poolConfig = new GenericObjectPoolConfig<T>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        // must set timeBetweenEvictionRunsMillis since eviction doesn't run unless that is positive
        poolConfig.setTimeBetweenEvictionRunsMillis(5L * 60L * 1000L);
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWaitMillis(3000);
        return poolConfig;
    }
}
