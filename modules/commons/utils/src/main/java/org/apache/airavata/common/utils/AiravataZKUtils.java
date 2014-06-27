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
package org.apache.airavata.common.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.util.List;

public class AiravataZKUtils {
    public static final String ZK_EXPERIMENT_STATE_NODE = "state";

    public static String getExpZnodePath(String experimentId, String taskId) throws ApplicationSettingsException {
        return ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE) +
                File.separator +
                ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME) + File.separator
                + experimentId + "+" + taskId;
    }

    public static String getExpZnodeHandlerPath(String experimentId, String taskId, String className) throws ApplicationSettingsException {
        return ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE) +
                File.separator +
                ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME) + File.separator
                + experimentId + "+" + taskId + File.separator + className;
    }

    public static String getZKhostPort() throws ApplicationSettingsException {
        return ServerSettings.getSetting(Constants.ZOOKEEPER_SERVER_HOST)
                + ":" + ServerSettings.getSetting(Constants.ZOOKEEPER_SERVER_PORT);
    }

    public static String getExpStatePath(String experimentId, String taskId) throws ApplicationSettingsException {
        return AiravataZKUtils.getExpZnodePath(experimentId, taskId) +
                File.separator +
                "state";
    }

    public static String getExpTokenId(ZooKeeper zk, String expId, String tId) throws ApplicationSettingsException,
            KeeperException, InterruptedException {
        Stat exists = zk.exists(getExpZnodePath(expId, tId), false);
        if (exists != null) {
            return new String(zk.getData(getExpZnodePath(expId, tId), false, exists));
        }
        return null;
    }

    public static String getExpState(ZooKeeper zk, String expId, String tId) throws ApplicationSettingsException,
            KeeperException, InterruptedException {
        Stat exists = zk.exists(getExpStatePath(expId, tId), false);
        if (exists != null) {
            return new String(zk.getData(getExpStatePath(expId, tId), false, exists));
        }
        return null;
    }

    public static List<String> getRunningGfacNodeNames(ZooKeeper zk) throws KeeperException, InterruptedException {
        String gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_API_SERVER_NODE, "/gfac-server");
        return zk.getChildren(gfacServer, null);
    }


    public static List<String> getAllGfacNodeNames(ZooKeeper zk) throws KeeperException, InterruptedException {
        String gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
        return zk.getChildren(gfacServer, null);
    }
}
