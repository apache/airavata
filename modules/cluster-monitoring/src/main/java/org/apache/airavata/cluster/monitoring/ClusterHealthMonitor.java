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
package org.apache.airavata.cluster.monitoring;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClusterHealthMonitor {
    private static final Logger logger = Logger.getLogger(ClusterHealthMonitor.class);

    public static void main(String[] args) throws IOException {

        byte[] publicKeyBytes = IOUtils.toByteArray(ClusterHealthMonitor.class.getResourceAsStream("/id_rsa.pub"));
        byte[] privateKeyBytes = IOUtils.toByteArray(ClusterHealthMonitor.class.getResourceAsStream("/id_rsa"));
        String passPhrase = "ultrascan";

        Gson gson = new Gson();
        List<ComputeResourceProfile> computeResourceProfiles = gson.fromJson(new FileReader(ClusterHealthMonitor.class
                .getResource("/cluster-properties.json").getFile()), new TypeToken<List<ComputeResourceProfile>>(){}.getType());

        ArrayList<QueueStatus> queueStatuses = new ArrayList<>();

        for(ComputeResourceProfile computeResourceProfile : computeResourceProfiles){

            String userName = computeResourceProfile.getUserName();
            String hostName = computeResourceProfile.getHostName();
            int port = computeResourceProfile.getPort();

            try{
                JSch jsch = new JSch();
                jsch.addIdentity(hostName, privateKeyBytes, publicKeyBytes, passPhrase.getBytes());

                Session session=jsch.getSession(userName, hostName, port);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);


                logger.debug("Connected to " + hostName);

                session.connect();
                for(String queue : computeResourceProfile.getQueueNames()) {
                    String command = "";
                    if (computeResourceProfile.getResourceManagerType().equals("SLURM"))
                        command = "sinfo -s -p " + queue + " -o \"%a %F\" | tail -1";
                    else if (computeResourceProfile.getResourceManagerType().equals("PBS"))
                        command = "qstat -Q " + queue + "| tail -1";

                    if (command.equals("")) {
                        logger.warn("No matching resource manager type found for " + computeResourceProfile.getResourceManagerType());
                        continue;
                    }

                    Channel channel = session.openChannel("exec");
                    ((ChannelExec) channel).setCommand(command);
                    channel.setInputStream(null);
                    ((ChannelExec) channel).setErrStream(System.err);
                    InputStream in = channel.getInputStream();
                    channel.connect();
                    byte[] tmp = new byte[1024];
                    String result = "";
                    while (true) {
                        while (in.available() > 0) {
                            int i = in.read(tmp, 0, 1024);
                            if (i < 0) break;
                            result += new String(tmp, 0, i);
                        }
                        if (channel.isClosed()) {
                            if (in.available() > 0) continue;
                            logger.debug(hostName + " " + queue + " " + "exit-status: " + channel.getExitStatus());
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ee) {
                        }
                    }
                    channel.disconnect();

                    if (result != null && result.length() > 0) {
                        QueueStatus queueStatus = null;
                        if (computeResourceProfile.getResourceManagerType().equals("SLURM")) {
                            String[] sparts = result.split(" ");
                            boolean isUp = sparts[0].equalsIgnoreCase("up");
                            String knts = sparts[1];
                            sparts = knts.split("/");
                            int running = Integer.parseInt(sparts[0].trim());
                            int queued = Integer.parseInt(sparts[1].trim());
                            queueStatus = new QueueStatus(hostName, queue, isUp, running, queued, System.currentTimeMillis());

                        } else if (computeResourceProfile.getResourceManagerType().equals("PBS")) {
                            result = result.replaceAll("\\s+", " ");
                            String[] sparts = result.split(" ");
                            boolean isUp = sparts[3].equalsIgnoreCase("yes");
                            int running = Integer.parseInt(sparts[6].trim());
                            int queued = Integer.parseInt(sparts[5].trim());
                            queueStatus = new QueueStatus(hostName, queue, isUp, running, queued, System.currentTimeMillis());
                        }

                        if (queueStatus != null)
                            queueStatuses.add(queueStatus);
                    }
                }
                session.disconnect();
            }catch (JSchException ex){
                logger.error(ex.getMessage(), ex);
            }
        }

        System.out.println(queueStatuses.size());

    }

    private static class ComputeResourceProfile{

        private String hostName;
        private String userName;
        private int port;
        private List<String> queueNames;
        private String resourceManagerType;

        public ComputeResourceProfile(String hostName, String userName, int port, List<String> queueNames, String resourceManagerType) {
            this.hostName = hostName;
            this.userName = userName;
            this.port = port;
            this.queueNames = queueNames;
            this.resourceManagerType = resourceManagerType;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public List<String> getQueueNames() {
            return queueNames;
        }

        public void setQueueNames(List<String> queueNames) {
            this.queueNames = queueNames;
        }

        public String getResourceManagerType() {
            return resourceManagerType;
        }

        public void setResourceManagerType(String resourceManagerType) {
            this.resourceManagerType = resourceManagerType;
        }
    }

    private static class QueueStatus{

        private String hostName;
        private String queueName;
        private boolean queueUp;
        private int runningJobs;
        private int queuedJobs;
        private long time;

        public QueueStatus(String hostName, String queueName, boolean queueUp, int runningJobs, int queuedJobs, long time) {
            this.hostName = hostName;
            this.queueName = queueName;
            this.queueUp = queueUp;
            this.runningJobs = runningJobs;
            this.queuedJobs = queuedJobs;
            this.time = time;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public boolean isQueueUp() {
            return queueUp;
        }

        public void setQueueUp(boolean queueUp) {
            this.queueUp = queueUp;
        }

        public int getRunningJobs() {
            return runningJobs;
        }

        public void setRunningJobs(int runningJobs) {
            this.runningJobs = runningJobs;
        }

        public int getQueuedJobs() {
            return queuedJobs;
        }

        public void setQueuedJobs(int queuedJobs) {
            this.queuedJobs = queuedJobs;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }
}