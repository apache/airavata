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
package org.apache.airavata.registry.repositories.appcatalog;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.registry.entities.appcatalog.SSHAccountProvisionerConfiguration;
import org.apache.airavata.registry.entities.appcatalog.SSHAccountProvisionerConfigurationPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SSHAccountProvisionerConfigurationRepository
        extends JpaRepository<SSHAccountProvisionerConfiguration, SSHAccountProvisionerConfigurationPK> {

    @Query(
            "SELECT s FROM SSHAccountProvisionerConfiguration s WHERE s.gatewayId = :gatewayId AND s.resourceId = :resourceId")
    List<SSHAccountProvisionerConfiguration> findByGatewayIdAndResourceId(
            @Param("gatewayId") String gatewayId, @Param("resourceId") String resourceId);

    default Map<String, String> getSshAccountProvisionerConfig(String gatewayId, String resourceId) {
        List<SSHAccountProvisionerConfiguration> configs = findByGatewayIdAndResourceId(gatewayId, resourceId);
        return configs.stream()
                .collect(Collectors.toMap(
                        SSHAccountProvisionerConfiguration::getConfigName,
                        SSHAccountProvisionerConfiguration::getConfigValue));
    }
}
