/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.file.server;

import java.time.Duration;
import org.apache.airavata.common.util.ThriftClientPool;
import org.apache.airavata.execution.orchestrator.AdaptorSupportImpl;
import org.apache.airavata.execution.orchestrator.AdaptorSupport;
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

    @Value("${airavata.server.host:localhost}")
    private String airavataServerHost;

    @Value("${airavata.server.port:8930}")
    private int airavataServerPort;

    @Bean
    public ThriftClientPool<RegistryService.Client> registryClientPool() {
        GenericObjectPoolConfig<RegistryService.Client> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMinutes(5));
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWait(Duration.ofSeconds(3));

        return new ThriftClientPool<>(
                RegistryService.Client::new, poolConfig, airavataServerHost, airavataServerPort, "RegistryService");
    }
}
