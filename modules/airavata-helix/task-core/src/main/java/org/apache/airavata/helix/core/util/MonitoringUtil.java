package org.apache.airavata.helix.core.util;

import org.apache.airavata.model.status.JobState;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MonitoringUtil {

    private final static Logger logger = LoggerFactory.getLogger(MonitoringUtil.class);

    private static final String MONITORING = "/monitoring/";
    private static final String REGISTRY = "/registry/";

    private static final String EXPERIMENT = "/experiment";
    private static final String TASK = "/task";
    private static final String PROCESS = "/process";
    private static final String GATEWAY = "/gateway";
    private static final String LOCK = "/lock";

    private static final String STATUS = "/status";
    private static final String JOBS = "/jobs";
    private static final String JOB_ID = "/jobId";
    private static final String JOB_NAME = "/jobName";
    private static final String WORKFLOWS = "/workflows";

    public static final String CANCEL = "cancel";

    public static String getExperimentIdByJobId(CuratorFramework curatorClient, String jobId) throws Exception {
        String path = MONITORING + jobId + EXPERIMENT;
        if (curatorClient.checkExists().forPath(path) != null) {
            byte[] processBytes = curatorClient.getData().forPath(path);
            return new String(processBytes);
        } else {
            return null;
        }
    }

    public static String getTaskIdByJobId(CuratorFramework curatorClient, String jobId) throws Exception {
        String path = MONITORING + jobId + TASK;
        if (curatorClient.checkExists().forPath(path) != null) {
            byte[] processBytes = curatorClient.getData().forPath(path);
            return new String(processBytes);
        } else {
            return null;
        }
    }

    public static String getProcessIdByJobId(CuratorFramework curatorClient, String jobId) throws Exception {
        String path = MONITORING + jobId + PROCESS;
        if (curatorClient.checkExists().forPath(path) != null) {
            byte[] processBytes = curatorClient.getData().forPath(path);
            return new String(processBytes);
        } else {
            return null;
        }
    }

    public static String getGatewayByJobId(CuratorFramework curatorClient, String jobId) throws Exception {
        String path = MONITORING + jobId + GATEWAY;
        if (curatorClient.checkExists().forPath(path) != null) {
            byte[] gatewayBytes = curatorClient.getData().forPath(path);
            return new String(gatewayBytes);
        } else {
            return null;
        }
    }

    public static void updateStatusOfJob(CuratorFramework curatorClient, String jobId, JobState jobState) throws Exception {
        String path = MONITORING + jobId + STATUS;
        if (curatorClient.checkExists().forPath(path) != null) {
            curatorClient.delete().forPath(path);
        }
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, jobState.name().getBytes());
    }

    public static JobState getCurrentStatusOfJob(CuratorFramework curatorClient, String jobId) throws Exception {
        String path = MONITORING + jobId + STATUS;
        if (curatorClient.checkExists().forPath(path) != null) {
            byte[] gatewayBytes = curatorClient.getData().forPath(path);
            return JobState.valueOf(new String(gatewayBytes));
        } else {
            return null;
        }
    }

    public static boolean hasMonitoringRegistered(CuratorFramework curatorClient, String jobId) throws Exception {
        Stat stat = curatorClient.checkExists().forPath(MONITORING + jobId);
        return stat != null;
    }

    // TODO perform exception handling
    @SuppressWarnings("WeakerAccess")
    public static void createMonitoringNode(CuratorFramework curatorClient, String jobId, String jobName, String taskId, String processId,
                                            String experimentId, String gateway) throws Exception {
        logger.info("Creating zookeeper paths for job monitoring for job id : " + jobId + ", process : "
                + processId + ", gateway : " + gateway);

        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                MONITORING + jobId + LOCK, new byte[0]);
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                MONITORING + jobId + GATEWAY, gateway.getBytes());
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                MONITORING + jobId + PROCESS, processId.getBytes());
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                MONITORING + jobId + TASK, taskId.getBytes());
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                MONITORING + jobId + EXPERIMENT, experimentId.getBytes());
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                MONITORING + jobId + JOB_NAME, jobName.getBytes());
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                MONITORING + jobName + JOB_ID, jobId.getBytes());
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                REGISTRY + processId + JOBS + "/" + jobId, new byte[0]);
    }

    public static void deleteMonitoringNode(CuratorFramework curatorClient, String jobId) {
        logger.info("Deleting zookeeper paths in job monitoring for job id : " + jobId);

    }

    public static void registerWorkflow(CuratorFramework curatorClient, String processId, String workflowId) throws Exception {
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                REGISTRY + processId + WORKFLOWS + "/" + workflowId , new byte[0]);
    }

    public static String getStatusOfProcess(CuratorFramework curatorClient, String processId) throws Exception {
        String path = REGISTRY + processId + STATUS;
        if (curatorClient.checkExists().forPath(path) != null) {
            byte[] statusBytes = curatorClient.getData().forPath(path);
            return new String(statusBytes);
        } else {
            return null;
        }
    }

    public static void registerCancelProcess(CuratorFramework curatorClient, String processId) throws Exception {
        String path = REGISTRY + processId + STATUS;
        if (curatorClient.checkExists().forPath(path) != null) {
            curatorClient.delete().forPath(path);
        }
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                path , CANCEL.getBytes());
    }

    public static List<String> getWorkflowsOfProcess(CuratorFramework curatorClient, String processId) throws Exception {
        String path = REGISTRY + processId + WORKFLOWS;
        if (curatorClient.checkExists().forPath(path) != null) {
            return curatorClient.getChildren().forPath(path);
        } else {
            return null;
        }
    }
}
