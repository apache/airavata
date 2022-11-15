package org.apache.airavata.compute.resource.monitoring;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputationalResourceMonitoringService implements IServer{

    private final static Logger logger = LoggerFactory.getLogger(ComputationalResourceMonitoringService.class);
    private static final String SERVER_NAME = "Airavata Compute Resource Monitoring Service";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public void restart() throws Exception {

    }

    @Override
    public void configure() throws Exception {

    }

    @Override
    public ServerStatus getStatus() throws Exception {
        return null;
    }
}
