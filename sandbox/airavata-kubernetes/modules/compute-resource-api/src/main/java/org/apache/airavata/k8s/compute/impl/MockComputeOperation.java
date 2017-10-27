package org.apache.airavata.k8s.compute.impl;

import org.apache.airavata.k8s.compute.api.ComputeOperations;
import org.apache.airavata.k8s.compute.api.ExecutionResult;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class MockComputeOperation implements ComputeOperations {

    private String computeHost;

    public MockComputeOperation(String computeHost) {
        this.computeHost = computeHost;
    }

    @Override
    public ExecutionResult executeCommand(String command) throws Exception {
        System.out.println("Executing command " + command + " on host " + this.computeHost);
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setStdOut("Sample standard out");
        executionResult.setStdErr("Simple standard error");
        Thread.sleep(5000);
        System.out.println("Command successfully executed");
        return executionResult;
    }

    @Override
    public void transferDataIn(String source, String target, String protocol) throws Exception {
        System.out.println("Transferring data in from " + source + " to " + target);
        Thread.sleep(5000);
        System.out.println("Transferred data in from " + source + " to " + target);
    }

    @Override
    public void transferDataOut(String source, String target, String protocol) throws Exception {
        System.out.println("Transferring data out from " + source + " to " + target);
        Thread.sleep(5000);
        System.out.println("Transferred data out from " + source + " to " + target);
    }
}
