package org.apache.airavata.k8s.compute.api;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface ComputeOperations {
    public ExecutionResult executeCommand(String command) throws Exception;
    public void transferDataIn(String source, String target, String protocol) throws Exception;
    public void transferDataOut(String source, String target, String protocol) throws Exception;
}
