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

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

public class AiravataZKUtils implements Watcher {
    private final static Logger logger = LoggerFactory.getLogger(AiravataZKUtils.class);

    public static final String ZK_EXPERIMENT_STATE_NODE = "state";

    public static final String DELIVERY_TAG_POSTFIX = "-deliveryTag";

    @Override
    public void process(WatchedEvent event) {

    }

    public static String getExpZnodePath(String experimentId) throws ApplicationSettingsException {
        return ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE) +
                File.separator +
                ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME) + File.separator
                + experimentId;
    }

    public static String getExpZnodeHandlerPath(String experimentId, String className) throws ApplicationSettingsException {
        return ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE) +
                File.separator +
                ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME) + File.separator
                + experimentId + File.separator + className;
    }

    public static String getZKhostPort() throws ApplicationSettingsException {
        return ServerSettings.getSetting(Constants.ZOOKEEPER_SERVER_HOST,"localhost")
                + ":" + ServerSettings.getSetting(Constants.ZOOKEEPER_SERVER_PORT,"2181");
    }

    public static int getZKTimeout()throws ApplicationSettingsException {
        return Integer.parseInt(ServerSettings.getSetting(Constants.ZOOKEEPER_TIMEOUT,"30000"));
    }

    public static String getExpStatePath(String experimentId) throws ApplicationSettingsException {
        return AiravataZKUtils.getExpZnodePath(experimentId) +
                File.separator +
                "state";
    }

    public static String getExpTokenId(ZooKeeper zk, String expId) throws ApplicationSettingsException,
            KeeperException, InterruptedException {
        Stat exists = zk.exists(getExpZnodePath(expId), false);
        if (exists != null) {
            return new String(zk.getData(getExpZnodePath(expId), false, exists));
        }
        return null;
    }

    public static String getExpState(ZooKeeper zk, String expId) throws ApplicationSettingsException,
            KeeperException, InterruptedException {
        Stat exists = zk.exists(getExpStatePath(expId), false);
        if (exists != null) {
            return new String(zk.getData(getExpStatePath(expId), false, exists));
        }
        return null;
    }


    public static int getExpStateValueWithGivenPath(ZooKeeper zk,String fullPath)throws ApplicationSettingsException,
            KeeperException, InterruptedException {
        Stat exists = zk.exists(fullPath, false);
        if (exists != null) {
            return Integer.parseInt(new String(zk.getData(fullPath, false, exists)));
        }
        return -1;
    }
    public static List<String> getRunningGfacNodeNames(ZooKeeper zk) throws KeeperException, InterruptedException {
        String gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_API_SERVER_NODE, "/gfac-server");
        return zk.getChildren(gfacServer, null);
    }


    public static List<String> getAllGfacNodeNames(ZooKeeper zk) throws KeeperException, InterruptedException {
        String gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
        return zk.getChildren(gfacServer, null);
    }

    public static void runZKFromConfig(ServerConfig config,ServerCnxnFactory cnxnFactory) throws IOException {
        AiravataZKUtils.logger.info("Starting Zookeeper server...");
        FileTxnSnapLog txnLog = null;
        try {
            // Note that this thread isn't going to be doing anything else,
            // so rather than spawning another thread, we will just call
            // run() in this thread.
            // create a file logger url from the command line args
            ZooKeeperServer zkServer = new ZooKeeperServer();

            txnLog = new FileTxnSnapLog(new File(config.getDataDir()), new File(
                    config.getDataDir()));
            zkServer.setTxnLogFactory(txnLog);
            zkServer.setTickTime(config.getTickTime());
            zkServer.setMinSessionTimeout(config.getMinSessionTimeout());
            zkServer.setMaxSessionTimeout(config.getMaxSessionTimeout());
            cnxnFactory = ServerCnxnFactory.createFactory();
            cnxnFactory.configure(config.getClientPortAddress(),
                    config.getMaxClientCnxns());
            cnxnFactory.startup(zkServer);
            cnxnFactory.join();
            if (zkServer.isRunning()) {
                zkServer.shutdown();
            }
        } catch (InterruptedException e) {
            // warn, but generally this is ok
            AiravataZKUtils.logger.warn("Server interrupted", e);
            System.exit(1);
        } finally {
            if (txnLog != null) {
                txnLog.close();
            }
        }
    }

    public static void startEmbeddedZK(ServerCnxnFactory cnxnFactory) {
        if (ServerSettings.isEmbeddedZK()) {
            ServerConfig serverConfig = new ServerConfig();
            URL resource = AiravataZKUtils.class.getClassLoader().getResource("zoo.cfg");
            if (resource == null) {
                logger.error("There is no zoo.cfg file in the classpath... Failed to start Zookeeper Server");
                System.exit(1);
            }
            try {
                serverConfig.parse(resource.getPath());
            } catch (QuorumPeerConfig.ConfigException e) {
                logger.error("Error while starting embedded Zookeeper", e);
                System.exit(2);
            }

            final ServerConfig fServerConfig = serverConfig;
            final ServerCnxnFactory fserverCnxnFactory = cnxnFactory;
            (new Thread() {
                public void run() {
                    try {
                        AiravataZKUtils.runZKFromConfig(fServerConfig,fserverCnxnFactory);
                    } catch (IOException e) {
                        logger.error("Error while starting embedded Zookeeper", e);
                        System.exit(3);
                    }
                }
            }).start();
        }else{
            logger.info("Skipping Zookeeper embedded startup ...");
        }
    }

    public static void storeDeliveryTag(ZooKeeper zk,String newExpNode,Double deliveryTag) throws KeeperException, InterruptedException {
        String s = zk.create(newExpNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        Stat expParent = zk.exists(newExpNode, false);
        if (expParent != null) {
            zk.setData(newExpNode, toByteArray(deliveryTag),
                    expParent.getVersion());
        }
    }

    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static long getDeliveryTag(String experimentID, ZooKeeper zk, String experimentNode,
                                      String pickedChild) throws KeeperException, InterruptedException,AiravataException {
        String deliveryTagPath = experimentNode + File.separator + pickedChild + File.separator + experimentID
                + DELIVERY_TAG_POSTFIX;
        Stat exists = zk.exists(deliveryTagPath, false);
        if(exists==null) {
            logger.error("Cannot find delivery Tag in path:" + deliveryTagPath + " for this experiment");
            return -1;
        }
        return bytesToLong(zk.getData(deliveryTagPath, false, exists));
    }
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }
}
