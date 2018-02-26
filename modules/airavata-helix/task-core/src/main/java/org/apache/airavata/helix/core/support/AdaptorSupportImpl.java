package org.apache.airavata.helix.core.support;

import org.apache.airavata.agents.api.*;
import org.apache.airavata.helix.agent.ssh.SshAgentAdaptor;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;

import java.io.File;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AdaptorSupportImpl implements AdaptorSupport {

    private static AdaptorSupportImpl INSTANCE;

    private final AgentStore agentStore = new AgentStore();

    private AdaptorSupportImpl() {}

    public synchronized static AdaptorSupportImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdaptorSupportImpl();
        }
        return INSTANCE;
    }

    public void initializeAdaptor() {
    }

    public AgentAdaptor fetchAdaptor(String gatewayId, String computeResource, String protocol, String authToken, String userId) throws AgentException {
        SshAgentAdaptor agentAdaptor = new SshAgentAdaptor();
        agentAdaptor.init(computeResource, gatewayId, userId, authToken);
        return agentAdaptor;
    }
}
