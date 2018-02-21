package org.apache.airavata.helix.workflow;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class SimpleWorkflow {

    public static void main(String[] args) throws Exception {
        WorkflowManager wm = new WorkflowManager("AiravataDemoCluster", "WorkflowManager", "localhost:2199");

        /*MkdirTask mkdirTask1 = new MkdirTask();
        mkdirTask1.setDirName("/tmp/newdir");
        mkdirTask1.setComputeResourceId("localhost");
        mkdirTask1.setTaskId("task1");

        MkdirTask mkdirTask2 = new MkdirTask();
        mkdirTask2.setDirName("/tmp/newdir2");
        mkdirTask2.setComputeResourceId("localhost");
        mkdirTask2.setTaskId("task2");

        CommandTask commandTask1 = new CommandTask();
        commandTask1.setCommand("touch /tmp/newdir/a.txt");
        commandTask1.setWorkingDirectory("/tmp");
        commandTask1.setComputeResource("localhost");
        commandTask1.setTaskId("task3");

        mkdirTask1.setSuccessPort(new OutPort("task2", mkdirTask1));
        mkdirTask2.setSuccessPort(new OutPort("task3", mkdirTask2));

        List<AbstractTask> allTasks = new ArrayList<>();
        allTasks.add(mkdirTask2);
        allTasks.add(mkdirTask1);
        allTasks.add(commandTask1);

        wm.launchWorkflow(UUID.randomUUID().toString(), allTasks);*/
    }
}
