package org.apache.airavata.apis.workflow.task.common;

import org.apache.airavata.apis.workflow.task.common.annotation.TaskOutPort;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskParam;
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskResult;
import org.apache.helix.task.UserContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BaseTask extends UserContentStore implements Task {

    private final static Logger logger = LoggerFactory.getLogger(BaseTask.class);

    private ThreadLocal<TaskCallbackContext> callbackContext = new ThreadLocal<>();
    private BlockingQueue<TaskCallbackContext> callbackContextQueue = new LinkedBlockingQueue<>();

    @TaskOutPort(name = "nextTask")
    private List<OutPort> outPorts = new ArrayList<>();

    @TaskParam(name = "taskId")
    private ThreadLocal<String> taskId = new ThreadLocal<>();

    @TaskParam(name = "retryCount")
    private ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(()-> 3);

    private ThreadLocal<Map<String,String>> paramOverrideMap = ThreadLocal.withInitial(() -> new HashMap<>());

    @Override
    public TaskResult run() {
        try {
            TaskCallbackContext cbc = callbackContextQueue.poll();

            if (cbc == null) {
                logger.error("No callback context available");
                throw new Exception("No callback context available");
            }

            this.callbackContext.set(cbc);
            String helixTaskId = getCallbackContext().getTaskConfig().getId();
            logger.info("Running task {}", helixTaskId);

            Map<String, String> configMap = getCallbackContext().getTaskConfig().getConfigMap();
            TaskUtil.deserializeTaskData(this, configMap);

            for (String key: configMap.keySet()) {
                if (key.startsWith("$")) {
                    String contextVariable = configMap.get(key);
                    String paramName = key.substring(1);
                    String paramValue = getUserContent(contextVariable, Scope.WORKFLOW);
                    Field cf = TaskUtil.getClassFieldForParamName(this, paramName);
                    TaskUtil.deserializeField(this, cf, paramValue);
                }
            }

        } catch (Exception e) {
            logger.error("Failed at deserializing task data", e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed in deserializing task data");
        }

        try {
            return onRun();
        } catch (Exception e) {
            logger.error("Unknown error while running task {}", getTaskId(), e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed due to unknown error");
        }
    }

    @Override
    public void cancel() {
        try {
            onCancel();
        } catch (Exception e) {
            logger.error("Unknown error while cancelling task {}", getTaskId(), e);
        }
    }

    public void overrideParameterFromWorkflowContext(String paramName, String contextVariable) {
        getParamOverrideMap().put(paramName, contextVariable);
    }

    public abstract TaskResult onRun() throws Exception;

    public abstract void onCancel() throws Exception;

    public List<OutPort> getOutPorts() {
        return outPorts;
    }

    public void addOutPort(OutPort outPort) {
        this.outPorts.add(outPort);
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    public void setRetryCount(int retryCount) {
        this.retryCount.set(retryCount);
    }

    public TaskCallbackContext getCallbackContext() {
        return callbackContext.get();
    }

    public String getTaskId() {
        return taskId.get();
    }

    public void setTaskId(String taskId) {
        this.taskId.set(taskId);
    }

    public Map<String, String> getParamOverrideMap() {
        return paramOverrideMap.get();
    }

    public void setParamOverrideMap(Map<String, String> paramOverrideMap) {
        this.paramOverrideMap.set(paramOverrideMap);
    }

    public void setCallbackContext(TaskCallbackContext callbackContext) {
        logger.info("Setting callback context {}", callbackContext.getJobConfig().getId());
        try {
            this.callbackContextQueue.put(callbackContext);
        } catch (InterruptedException e) {
            logger.error("Failed to put callback context to the queue", e);
        }
    }
}
