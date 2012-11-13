package org.apache.airavata.services.registry.rest.utils;

import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;

//import org.apache.airavata.client.AiravataClient;
//import org.apache.airavata.client.AiravataClientUtils;
//import org.apache.airavata.client.api.AiravataAPI;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class RegistryListener implements ServletContextListener {
    private static AiravataRegistry2 airavataRegistry;

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            ServletContext servletContext = servletContextEvent.getServletContext();

            URL url = this.getClass().getClassLoader().
                    getResource(RestServicesConstants.AIRAVATA_SERVER_PROPERTIES);
            Properties properties = new Properties();
            try {
                properties.load(url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String gatewayID = properties.getProperty(RestServicesConstants.GATEWAY_ID);
            String registryUser = properties.getProperty(RestServicesConstants.REGISTRY_USERNAME);
            Gateway gateway =  new Gateway(gatewayID);
            AiravataUser airavataUser = new AiravataUser(registryUser) ;

            airavataRegistry = AiravataRegistryFactory.getRegistry(gateway, airavataUser);
            servletContext.setAttribute(RestServicesConstants.AIRAVATA_REGISTRY, airavataRegistry);
            servletContext.setAttribute(RestServicesConstants.GATEWAY, gateway);
            servletContext.setAttribute(RestServicesConstants.REGISTRY_USER, airavataUser);

//            AiravataAPI airavataAPI = AiravataClientUtils.getAPI(url.getPath());
//            servletContext.setAttribute(RestServicesConstants.AIRAVATA_API, airavataAPI);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
