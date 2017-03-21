/*
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
 *
*/
package org.apache.airavata.db.event.manager.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goshenoy on 3/21/17.
 */
public class DbEventManagerZkUtils {

    public static void createDBEventMgrZkNode(CuratorFramework curatorClient, String publisherNode, String subscriberNode) throws Exception {
        // construct ZK paths for pub,sub
        String publisherZkPath = ZKPaths.makePath(Constants.DB_EVENT_MGR_ZK_PATH, publisherNode);
        String subscriberZkPath = ZKPaths.makePath(publisherZkPath, subscriberNode);

        // construct byte-data(s) for pub, sub
        byte[] publisherZkData = subscriberNode.getBytes();
        byte[] subscriberZkData = subscriberNode.getBytes();

        // create zkNode: "/db-event-mgr/pubnodename/subnodename"
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), subscriberZkPath);

        // set zkNode data for pub,sub
        curatorClient.setData().withVersion(-1 ).forPath(publisherZkPath, publisherZkData);
        curatorClient.setData().withVersion(-1 ).forPath(subscriberZkPath, subscriberZkData);
    }

    public static Map<String, List<String>> getSubscribersForPublisher(CuratorFramework curatorClient, String publisherNode) throws Exception {
        Map<String, List<String>> subscriberMap = new HashMap<>();

        // construct ZK path for pub
        String publisherZkPath = ZKPaths.makePath(Constants.DB_EVENT_MGR_ZK_PATH, publisherNode);

        // get children-list for pub
        List<String> subscriberList = curatorClient.getChildren().forPath(publisherZkPath);

        subscriberMap.put(publisherNode, subscriberList);
        return subscriberMap;
    }

//    public static void main(String[] args) {
//        String connectionString = "localhost:2181";
//        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
//
//        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
//        curatorClient.start();
//        try {
//            DbEventManagerZkUtils.createDBEventMgrZkNode(curatorClient, "userprofile", "sharing");
//            DbEventManagerZkUtils.createDBEventMgrZkNode(curatorClient, "userprofile", "appcatalog");
//            System.out.println(DbEventManagerZkUtils.getSubscribersForPublisher(curatorClient, "userprofile"));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}
