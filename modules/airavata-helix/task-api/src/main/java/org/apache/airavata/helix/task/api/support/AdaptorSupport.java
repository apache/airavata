package org.apache.airavata.helix.task.api.support;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.JobSubmissionOutput;

import java.io.File;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface AdaptorSupport {
    public void initializeAdaptor();

    public AgentAdaptor fetchAdaptor(String gatewayId, String computeResource, String protocol, String authToken, String userId) throws Exception;

}
