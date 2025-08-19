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

import org.apache.airavata.factory.AiravataServiceFactory;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.registry.api.RegistryService;
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

    // api.server.host
    @Value("${api.server.host:localhost}")
    private String registryServerHost;
    
    // api.server.port
    @Value("${api.server.port:8970}")
    private int registryServerPort;

    @Bean
    public RegistryService.Iface registry() {
        return AiravataServiceFactory.getRegistry();
    }
}
