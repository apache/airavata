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
package org.apache.airavata.sharing.registry.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ServerMain {
    private final static Logger logger = LoggerFactory.getLogger(ServerMain.class);

    private static long serverPID = -1;
    private static final String stopFileNamePrefix = "server_stop";
    private static final String serverStartedFileNamePrefix = "server_start";

    public static void main(String[] args) {
        try {
            setServerStarted();
            new SharingRegistryServer().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"resource"})
    private static void setServerStarted() {
        try {
            serverPID = getPID();
            deleteOldStopRequests();
            File serverStartedFile = null;
            serverStartedFile = new File(getServerStartedFileName());
            serverStartedFile.createNewFile();
            serverStartedFile.deleteOnExit();
            new RandomAccessFile(serverStartedFile, "rw").getChannel().lock();
        } catch (FileNotFoundException e) {
            logger.warn(e.getMessage(), e);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private static String getServerStartedFileName() {
        String SHARING_REGISTRY_HOME = System.getenv("" +"SHARING_REGISTRY_HOME");
        if(SHARING_REGISTRY_HOME==null)
            SHARING_REGISTRY_HOME = "/tmp";
        else
            SHARING_REGISTRY_HOME = SHARING_REGISTRY_HOME + "/bin";
        return new File(SHARING_REGISTRY_HOME, serverStartedFileNamePrefix + "_" + Long.toString(serverPID)).toString();
    }

//    private static int getPID() {
//        try {
//            java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory
//                    .getRuntimeMXBean();
//            java.lang.reflect.Field jvm = runtime.getClass()
//                    .getDeclaredField("jvm");
//            jvm.setAccessible(true);
//            sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm
//                    .get(runtime);
//            java.lang.reflect.Method pid_method = mgmt.getClass()
//                    .getDeclaredMethod("getProcessId");
//            pid_method.setAccessible(true);
//
//            int pid = (Integer) pid_method.invoke(mgmt);
//            return pid;
//        } catch (Exception e) {
//            return -1;
//        }
//    }

    //getPID from ProcessHandle JDK 9 and onwards
    private static long getPID () {
        try {
            return ProcessHandle.current().pid();
        } catch (Exception e) {
            return -1;
        }

    }

    private static void deleteOldStopRequests() {
        File[] files = new File(".").listFiles();
        for (File file : files) {
            if (file.getName().contains(stopFileNamePrefix)) {
                file.delete();
            }

        }
    }
}