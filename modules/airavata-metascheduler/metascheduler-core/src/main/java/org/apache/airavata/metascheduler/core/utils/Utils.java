package org.apache.airavata.metascheduler.core.utils;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.TException;


/**
 * This class contains all utility methods across scheduler sub projects
 */
public class Utils {

    private static ThriftClientPool<RegistryService.Client> registryClientPool;
    private static Publisher statusPublisher;

    /**
     * Provides registry client to access databases
     *
     * @return RegistryService.Client
     */
    public static synchronized ThriftClientPool<RegistryService.Client> getRegistryServiceClientPool() {
        if (registryClientPool != null) {
            return registryClientPool;
        }
        try {
//            final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
//            final String serverHost = ServerSettings.getRegistryServerHost();
            registryClientPool = new ThriftClientPool<>(
                    tProtocol -> new RegistryService.Client(tProtocol),
                    Utils.<RegistryService.Client>createGenericObjectPoolConfig(),
                    ServerSettings.getRegistryServerHost(),
                    ServerSettings.getRegistryServerPort());
            return registryClientPool;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }

    private static <T> GenericObjectPoolConfig<T> createGenericObjectPoolConfig() {

        GenericObjectPoolConfig<T> poolConfig = new GenericObjectPoolConfig<T>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        // must set timeBetweenEvictionRunsMillis since eviction doesn't run unless that is positive
        poolConfig.setTimeBetweenEvictionRunsMillis(5L * 60L * 1000L);
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWaitMillis(3000);
        return poolConfig;
    }

    public static void saveAndPublishProcessStatus(ProcessState processState, String processId,
                                                   String experimentId, String gatewayId)
            throws RegistryServiceException, TException, AiravataException {

        ProcessStatus processStatus = new ProcessStatus(processState);
        processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());

        registryClientPool.getResource().addProcessStatus(processStatus, processId);
        ProcessIdentifier identifier = new ProcessIdentifier(processId, experimentId, gatewayId);
        ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent(processState, identifier);
        MessageContext msgCtx = new MessageContext(processStatusChangeEvent, MessageType.PROCESS,
                AiravataUtils.getId(MessageType.PROCESS.name()), gatewayId);
        msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        getStatusPublisher().publish(msgCtx);
    }

    public static synchronized Publisher getStatusPublisher() throws AiravataException {
        if (statusPublisher == null) {
            statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
        }
        return statusPublisher;
    }
}
