package org.apache.airavata.integration.clients;

import org.apache.airavata.integration.utils.ConnectorUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.util.Properties;

public abstract class Connector {

    private Properties properties;

    public Connector(String fileName) throws IOException {

      this.properties =   ConnectorUtils.loadProperties(fileName);
    }


    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
