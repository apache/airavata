package org.apache.airavata.persistance.registry.jpa.resources;


import org.apache.airavata.registry.api.AiravataRegistryConnectionDataProvider;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.UnknownRegistryConnectionDataException;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class AiravataRegistryConnectionDataProviderImpl implements AiravataRegistryConnectionDataProvider {

    public static Properties loadProperties(){
        URL resource = Utils.class.getClassLoader().getResource("repository.properties");
        Properties properties = new Properties();
        try {
            properties.load(resource.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }


    public void setIdentity(Gateway gateway, AiravataUser use) {
    }


    public Object getValue(String key) throws UnknownRegistryConnectionDataException {
        return loadProperties().getProperty(key);
    }
}
