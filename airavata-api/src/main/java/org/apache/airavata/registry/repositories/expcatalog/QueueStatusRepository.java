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
package org.apache.airavata.registry.repositories.expcatalog;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.registry.entities.expcatalog.QueueStatusEntity;
import org.apache.airavata.registry.entities.expcatalog.QueueStatusPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueStatusRepository extends JpaRepository<QueueStatusEntity, QueueStatusPK> {

    @Query(
            "SELECT q FROM QueueStatusEntity q WHERE q.hostName LIKE :hostName AND q.queueName LIKE :queueName ORDER BY q.time DESC")
    List<QueueStatusEntity> findByHostNameAndQueueNameOrderByTimeDesc(
            @Param("hostName") String hostName, @Param("queueName") String queueName);

    @Query(
            "SELECT q FROM QueueStatusEntity q WHERE q.hostName LIKE :hostName AND q.queueName LIKE :queueName ORDER BY q.time DESC")
    Optional<QueueStatusEntity> findFirstByHostNameAndQueueNameOrderByTimeDesc(
            @Param("hostName") String hostName, @Param("queueName") String queueName);
}
