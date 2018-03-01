package org.apache.airavata.helix.task.api.support;

import org.apache.airavata.agents.api.*;

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
    public StorageResourceAdaptor fetchStorageAdaptor(String gatewayId, String storageResourceId, String protocol,  String authToken, String userId) throws AgentException;

}
