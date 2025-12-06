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
package org.apache.airavata.manager.dbevent.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDbEventManagerZkUtils {

    private static final Logger logger = LoggerFactory.getLogger(TestDbEventManagerZkUtils.class);

    public static void main(String[] args) {
        logger.info("TestDbEventManagerZkUtils::main()");
        // String connectionString = "localhost:2181";
        // String userProfileService =
        // DBEventManagerConstants.DBEventService.USER_PROFILE.toString();
        // String sharingService =
        // DBEventManagerConstants.DBEventService.SHARING.toString();
        // String registryService =
        // DBEventManagerConstants.DBEventService.REGISTRY.toString();
        // RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        // CuratorFramework curatorClient =
        // CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        // curatorClient.start();
        // try {
        // DbEventManagerZkUtils.createDBEventMgrZkNode(curatorClient,
        // userProfileService, sharingService);
        // DbEventManagerZkUtils.createDBEventMgrZkNode(curatorClient,
        // userProfileService, registryService);
        // logger.info("Subscribers: {}",
        // DbEventManagerZkUtils.getSubscribersForPublisher(curatorClient,
        // userProfileService));
        // } catch (Exception ex) {
        // logger.error("Error creating ZK node", ex);
        // }
    }
}
