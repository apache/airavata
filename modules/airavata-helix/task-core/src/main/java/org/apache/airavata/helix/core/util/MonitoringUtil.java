package org.apache.airavata.helix.core.util;

import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MonitoringUtil {

    private final static Logger logger = LoggerFactory.getLogger(MonitoringUtil.class);

    private static final String PATH_PREFIX = "/airavata";
    private static final String REGISTRY = "/registry/";

    private static final String TASK = "/task";

    private static final String JOBS = "/jobs";
    private static final String WORKFLOWS = "/workflows";
    private static final String RETRY = "/retry";

    public static void registerWorkflow(CuratorFramework curatorClient, String processId, String workflowId) throws Exception {
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                PATH_PREFIX + REGISTRY + processId + WORKFLOWS + "/" + workflowId , new byte[0]);
    }

    public static int getTaskRetryCount(CuratorFramework curatorClient, String taskId) throws Exception {
        String path = PATH_PREFIX + TASK + "/" + taskId + RETRY;
        if (curatorClient.checkExists().forPath(path) != null) {
            byte[] processBytes = curatorClient.getData().forPath(path);
            return Integer.parseInt(new String(processBytes));
        } else {
            return 1;
        }
    }

    public static void increaseTaskRetryCount(CuratorFramework curatorClient, String takId, int currentRetryCount) throws Exception {
        String path = PATH_PREFIX + TASK + "/" + takId + RETRY;
        if (curatorClient.checkExists().forPath(path) != null) {
            curatorClient.delete().forPath(path);
        }
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                path , ((currentRetryCount + 1) + "").getBytes());
    }

    public static List<String> getWorkflowsOfProcess(CuratorFramework curatorClient, String processId) throws Exception {
        String path = PATH_PREFIX + REGISTRY + processId + WORKFLOWS;
        if (curatorClient.checkExists().forPath(path) != null) {
            return curatorClient.getChildren().forPath(path);
        } else {
            return null;
        }
    }

    private static void deleteIfExists(CuratorFramework curatorClient, String path) throws Exception {
        if (curatorClient.checkExists().forPath(path) != null) {
            curatorClient.delete().deletingChildrenIfNeeded().forPath(path);
        }
    }

    public static void deleteTaskSpecificNodes(CuratorFramework curatorClient, String takId) throws Exception {
        deleteIfExists(curatorClient, PATH_PREFIX + TASK + "/" + takId + RETRY);
    }

    public static void deleteProcessSpecificNodes(CuratorFramework curatorClient, String processId) throws Exception {

        deleteIfExists(curatorClient, PATH_PREFIX + REGISTRY + processId + JOBS);
        deleteIfExists(curatorClient, PATH_PREFIX + REGISTRY + processId + WORKFLOWS);
        deleteIfExists(curatorClient, PATH_PREFIX + REGISTRY + processId);
    }
}
