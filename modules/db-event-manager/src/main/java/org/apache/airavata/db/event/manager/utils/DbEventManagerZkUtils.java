/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.db.event.manager.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by goshenoy on 3/21/17.
 */
public class DbEventManagerZkUtils {

    private static final Logger logger = LoggerFactory.getLogger(DbEventManagerZkUtils.class);
    private static CuratorFramework curatorClient;

    /**
     *  Get curatorFramework instance
     * @return
     * @throws ApplicationSettingsException
     */
    public static CuratorFramework getCuratorClient() throws ApplicationSettingsException {
        if (curatorClient == null) {
            synchronized (DbEventManagerZkUtils.class) {
                if (curatorClient == null) {
                    String connectionSting = ServerSettings.getZookeeperConnection();
                    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
                    curatorClient = CuratorFrameworkFactory.newClient(connectionSting, retryPolicy);
                }
            }
        }

        return curatorClient;
    }

    /**
     * Create Zk node for db event manager
     * @param curatorClient
     * @param publisherNode
     * @param subscriberNode
     * @throws Exception
     */
    public static void createDBEventMgrZkNode(CuratorFramework curatorClient, String publisherNode, String subscriberNode) throws Exception {
        // get pub,sub queue names

        // construct ZK paths for pub,sub
        String publisherZkPath = ZKPaths.makePath(Constants.DB_EVENT_MGR_ZK_PATH, publisherNode);
        String subscriberZkPath = ZKPaths.makePath(publisherZkPath, subscriberNode);

        // construct byte-data(s) for pub, sub
        byte[] publisherZkData = publisherNode.getBytes();
        byte[] subscriberZkData = subscriberNode.getBytes();

        // create zkNode: "/db-event-mgr/pubqueuename/subqueueename"
        logger.debug("Creating Zk node for db-event-mgr: " + subscriberZkPath);
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), subscriberZkPath);

        // set zkNode data for pub,sub
        curatorClient.setData().withVersion(-1 ).forPath(publisherZkPath, publisherZkData);
        curatorClient.setData().withVersion(-1 ).forPath(subscriberZkPath, subscriberZkData);
    }

    /**
     * Get list of subscribers for given publisher
     * @param curatorClient
     * @param publisherNode
     * @return
     * @throws Exception
     */
    public static List<String> getSubscribersForPublisher(CuratorFramework curatorClient, String publisherNode) throws Exception {

        // construct ZK path for pub
        String publisherZkPath = ZKPaths.makePath(Constants.DB_EVENT_MGR_ZK_PATH, publisherNode);

        // get children-list for pub
        List<String> subscriberList = curatorClient.getChildren().forPath(publisherZkPath);

        return subscriberList;
    }

//    public static void main(String[] args) {
//        String connectionString = "localhost:2181";
//        String userProfileService = DBEventManagerConstants.DBEventService.USER_PROFILE.toString();
//        String sharingService = DBEventManagerConstants.DBEventService.SHARING.toString();
//        String registryService = DBEventManagerConstants.DBEventService.REGISTRY.toString();
//
//        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
//
//        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
//        curatorClient.start();
//        try {
//            DbEventManagerZkUtils.createDBEventMgrZkNode(curatorClient, userProfileService, sharingService);
//            DbEventManagerZkUtils.createDBEventMgrZkNode(curatorClient, userProfileService, registryService);
//            System.out.println(DbEventManagerZkUtils.getSubscribersForPublisher(curatorClient, userProfileService));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}
