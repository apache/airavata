package org.apache.airavata.migrator.registry;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.StringParameterType;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XRegistryMigrate {
    private static String propertyfile = "xregistry.properties";

    public static void main(String[] args) throws XRegistryClientException {
        /* Create database */
        Map<String,String> config = new HashMap<String,String>();
            config.put("org.apache.jackrabbit.repository.home","target");
        AiravataJCRRegistry jcrRegistry = null;
        try {
            jcrRegistry = new AiravataJCRRegistry(null,
                    "org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
                    "admin", config);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        XRegistryClient client = XRegistryClientUtil.CreateGSISecureRegistryInstance("xregistry.properties");

        HostDescData[] hostDescs = client.findHosts("");
        Map<QName, HostDescData> val = new HashMap<QName, HostDescData>();
		for (HostDescData hostDesc : hostDescs) {
			val.put(hostDesc.getName(), hostDesc);
            String hostDescStr = client.getHostDesc(hostDesc.getName().getLocalPart());
            System.out.println(hostDescStr);
            HostBean hostBean = null;
            try {
                hostBean = org.ogce.schemas.gfac.beans.utils.HostUtils.simpleHostBeanRequest(hostDescStr);
            } catch (XmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if(hostBean != null){
//                hostBean.
            }

		}

        FindAppDescResponseDocument.FindAppDescResponse.AppData[] appDatas = client.findAppDesc("");
        Map<QName, FindAppDescResponseDocument.FindAppDescResponse.AppData> val2 = new HashMap<QName, FindAppDescResponseDocument.FindAppDescResponse.AppData>();
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
		}


        ServiceDescData[] serviceDescDatas = client.findServiceDesc("");
        Map<QName, ServiceDescData> val3 = new HashMap<QName, ServiceDescData>();
		for (ServiceDescData serviceDesc : serviceDescDatas) {
			val3.put(serviceDesc.getName(), serviceDesc);
            String serviceDescStr = client.getServiceDesc(serviceDesc.getName());
            System.out.println(serviceDescStr);
            ServiceBean serviceBean = null;
            try {
                serviceBean = org.ogce.schemas.gfac.beans.utils.ServiceUtils.serviceBeanRequest(serviceDescStr);
            } catch (XmlException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        /*
		 * Save to registry
		 */
		/*jcrRegistry.saveHostDescription(host);
		jcrRegistry.saveDeploymentDescription(serv.getType().getName(), host
				.getType().getHostName(), appDesc);
		jcrRegistry.saveServiceDescription(serv);
		jcrRegistry.deployServiceOnHost(serv.getType().getName(), host
				.getType().getHostName());*/

    }

    private HostDescription createHostDescription(String hostName, String hostAddress) {
        HostDescription host = new HostDescription();
        host.getType().setHostName(hostName);
        host.getType().setHostAddress(hostAddress);
        return host;
    }

    private ServiceDescription createServiceDescription() {
		ServiceDescription serv = new ServiceDescription();
		serv.getType().setName("SimpleEcho");

		List<InputParameterType> inputList = new ArrayList<InputParameterType>();
		InputParameterType input = InputParameterType.Factory.newInstance();
		input.setParameterName("echo_input");
		input.setParameterType(StringParameterType.Factory.newInstance());
		inputList.add(input);
		InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
				.size()]);

		List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
		OutputParameterType output = OutputParameterType.Factory.newInstance();
		output.setParameterName("echo_output");
		output.setParameterType(StringParameterType.Factory.newInstance());
		outputList.add(output);
		OutputParameterType[] outputParamList = outputList
				.toArray(new OutputParameterType[outputList.size()]);

		serv.getType().setInputParametersArray(inputParamList);
		serv.getType().setOutputParametersArray(outputParamList);
        return serv;
    }

    private ApplicationDeploymentDescription createAppDeploymentDescription() {
        ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType app = appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory
                .newInstance();
        name.setStringValue("EchoLocal");
        app.setApplicationName(name);
        app.setExecutableLocation("/bin/echo");
        app.setScratchWorkingDirectory("/tmp");
        app.setStaticWorkingDirectory("/tmp");
        app.setInputDataDirectory("/tmp/input");
        app.setOutputDataDirectory("/tmp/output");
        app.setStandardOutput("/tmp/echo.stdout");
        app.setStandardError("/tmp/echo.stdout");
        return appDesc;
    }

}

