package org.apache.airavata.migrator.registry;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.xmlbeans.XmlException;
import org.ogce.schemas.gfac.beans.ApplicationBean;
import org.ogce.schemas.gfac.beans.HostBean;
import org.ogce.schemas.gfac.beans.ServiceBean;
import org.ogce.xregistry.client.XRegistryClient;
import org.ogce.xregistry.client.XRegistryClientUtil;
import org.ogce.xregistry.utils.XRegistryClientException;
import xregistry.generated.FindAppDescResponseDocument;
import xregistry.generated.HostDescData;
import xregistry.generated.ServiceDescData;

import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XRegistryMigrate {
//    private static String propertyfile = "xregistry.properties";
//    private static String propertyfile = "xregistry-dropbox.properties";
    private static String propertyfile = "xregistry-local.properties";
    private static AiravataJCRRegistry jcrRegistry = null;

    private static String jcrRegsitryURL = null;
    private static String jcrUsername = null;
    private static String jcrPassword = null;

    public static void main(String[] args) throws XRegistryClientException {
        try {
            loadProperties(propertyfile);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        /* Create database */
        Map<String,String> config = new HashMap<String,String>();
        config.put("org.apache.jackrabbit.repository.home","target");
        try {
            jcrRegistry = new AiravataJCRRegistry(new URI(jcrRegsitryURL),
                    "org.apache.jackrabbit.core.RepositoryFactoryImpl",
                    jcrUsername, jcrPassword, config);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        XRegistryClient client = XRegistryClientUtil.CreateGSISecureRegistryInstance(propertyfile);
        saveAllHostDescriptions(client);
        saveAllServiceDescriptions(client);

        System.out.println("DONE!");
    }

    private static void loadProperties(String file) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream propertyStream = classLoader.getResourceAsStream(file);
        Properties properties = new Properties();
        if (propertyStream == null) {
            FileInputStream fileInputStream = new FileInputStream(new File(file));
            properties.load(fileInputStream);
        } else {
            properties.load(propertyStream);
        }

        jcrRegsitryURL = properties.getProperty(MigrationConstants.JCR_URL);
        jcrUsername = properties.getProperty(MigrationConstants.JCR_USERNAME);
        jcrPassword = properties.getProperty(MigrationConstants.JCR_PASSWORD);
        System.out.println(jcrRegsitryURL);
        System.out.println(jcrUsername);
        System.out.println(jcrPassword);

    }

    private static void saveAllHostDescriptions(XRegistryClient client) throws XRegistryClientException {
        HostDescription host = null;
        HostDescData[] hostDescs = client.findHosts("");
        Map<QName, HostDescData> val = new HashMap<QName, HostDescData>();
        for (HostDescData hostDesc : hostDescs) {
            val.put(hostDesc.getName(), hostDesc);
            String hostDescStr = client.getHostDesc(hostDesc.getName().getLocalPart());
            HostBean hostBean = null;
            try {
                hostBean = org.ogce.schemas.gfac.beans.utils.HostUtils.simpleHostBeanRequest(hostDescStr);
                System.out.println("Host : " + hostBean.getHostName());
                System.out.println(hostDescStr);

            } catch (XmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if(hostBean != null){
                host = MigrationUtil.createHostDescription(hostBean);
            }

            try {
                jcrRegistry.saveHostDescription(host);
            } catch (RegistryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        System.out.println("=== All Hosts are saved ===");
    }

    private static void saveAllServiceDescriptions(XRegistryClient client) throws XRegistryClientException {
        ServiceDescription service = null;
        ServiceDescData[] serviceDescDatas = client.findServiceDesc("");
        Map<QName, ServiceDescData> val3 = new HashMap<QName, ServiceDescData>();

        for (ServiceDescData serviceDesc : serviceDescDatas) {
            val3.put(serviceDesc.getName(), serviceDesc);
            String serviceDescStr = client.getServiceDesc(serviceDesc.getName());
            ServiceBean serviceBean = null;
            String applicationName = null;

            try {
                serviceBean = org.ogce.schemas.gfac.beans.utils.ServiceUtils.serviceBeanRequest(serviceDescStr);
                applicationName = serviceBean.getApplicationName();
                System.out.println("Service : " + serviceBean.getServiceName());
                System.out.println(serviceDescStr);
            } catch (XmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if(serviceBean != null) {
                service = MigrationUtil.createServiceDescription(serviceBean);
                try {
                    jcrRegistry.saveServiceDescription(service);
                    ApplicationBean appBean = saveApplicationDescriptionWithName(client, applicationName, service);
                    // TODO : should look into this
                    if (appBean != null){
                        jcrRegistry.deployServiceOnHost(service.getType().getName(), appBean.getHostName());
                    }
                } catch (RegistryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

        }
        System.out.println("=== All Service/Applciation descriptors are saved ===");
    }

    private static ApplicationBean saveApplicationDescriptionWithName(XRegistryClient client, String applicationName, ServiceDescription service) throws XRegistryClientException {
        ApplicationDeploymentDescription app = null;
        FindAppDescResponseDocument.FindAppDescResponse.AppData[] appDatas = client.findAppDesc(applicationName);
        Map<QName, FindAppDescResponseDocument.FindAppDescResponse.AppData> val2 =
                new HashMap<QName, FindAppDescResponseDocument.FindAppDescResponse.AppData>();
        ApplicationBean appBean = null;
        for (FindAppDescResponseDocument.FindAppDescResponse.AppData appDesc : appDatas) {
            val2.put(appDesc.getName(), appDesc);
            String appDescStr = client.getAppDesc(appDesc.getName().toString(),appDesc.getHostName());
            try {
                appBean = org.ogce.schemas.gfac.beans.utils.ApplicationUtils.simpleApplicationBeanRequest(appDescStr);
                System.out.println("Application : " + appBean.getApplicationName());
                System.out.println(appDescStr);

            } catch (XmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if(appBean != null){
                app = MigrationUtil.createAppDeploymentDescription(appBean);
                try {
                    jcrRegistry.saveDeploymentDescription(service.getType().getName(), appBean.getHostName(), app);
//            jcrRegistry.saveDeploymentDescription(service.getType().getName(), host.getType().getHostName(), app);
                } catch (RegistryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }


        }

        return appBean;
    }

    private static void saveAllApplicationDescriptions(XRegistryClient client, ServiceDescription service) throws XRegistryClientException {
        ApplicationDeploymentDescription app = null;
        FindAppDescResponseDocument.FindAppDescResponse.AppData[] appDatas = client.findAppDesc("");
        Map<QName, FindAppDescResponseDocument.FindAppDescResponse.AppData> val2 =
                new HashMap<QName, FindAppDescResponseDocument.FindAppDescResponse.AppData>();
        for (FindAppDescResponseDocument.FindAppDescResponse.AppData appDesc : appDatas) {
            val2.put(appDesc.getName(), appDesc);
            String appDescStr = client.getAppDesc(appDesc.getName().toString(),appDesc.getHostName());
            System.out.println(appDescStr);
            ApplicationBean appBean = null;
            try {
                appBean = org.ogce.schemas.gfac.beans.utils.ApplicationUtils.simpleApplicationBeanRequest(appDescStr);
            } catch (XmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if(appBean != null){
                app = MigrationUtil.createAppDeploymentDescription(appBean);
            }

            //jcrRegistry.saveDeploymentDescription(service.getType().getName(), host.getType().getHostName(), app);

        }
    }

}

