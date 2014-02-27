/*
 *
 * Licensed to the Apache Software Foundation (ASF) und= nuer one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.xbaya.invoker;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.credential.store.store.CredentialReaderFactory;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.gfac.cpi.GFacImpl;
import org.apache.airavata.gfac.ec2.AmazonSecurityContext;
import org.apache.airavata.gfac.scheduler.HostScheduler;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.schemas.gfac.*;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.apache.airavata.schemas.wec.SecurityContextDocument;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.jython.lib.ServiceNotifiable;
import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;

import xsul.wsdl.WsdlDefinitions;
import xsul.wsif.WSIFMessage;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.xwsif_runtime.WSIFClient;

public class EmbeddedGFacInvoker implements Invoker {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedGFacInvoker.class);

    private String nodeID;

    private QName portTypeQName;

    private String wsdlLocation;

    private String serviceInformation;

    private String messageBoxURL;

    private String gfacURL;

    private Invoker invoker;

    private XBayaConfiguration configuration;


    private Boolean result;

    private ServiceNotifiable notifier;

//    private AiravataRegistry2 registry;

    private String topic;

    private String serviceName;

    private AiravataAPI airavataAPI;
    /**
     * used for notification
     */
    private List<Object> inputValues = new ArrayList<Object>();

    /**
     * used for notification
     */
    private List<String> inputNames = new ArrayList<String>();

    boolean failerSent;

    private WsdlDefinitions wsdlDefinitionObject;

    private Object outPut;

    Map<String, Object> actualParameters = new LinkedHashMap<String, Object>();

    /**
     * Creates an InvokerWithNotification.
     *
     * @param portTypeQName
     * @param wsdlLocation  The URL of WSDL of the service to invoke
     * @param nodeID        The ID of the service
     * @param notifier      The notification sender
     */
    public EmbeddedGFacInvoker(QName portTypeQName, String wsdlLocation, String nodeID, WorkflowNotifiable notifier) {
        this(portTypeQName, wsdlLocation, nodeID, null, notifier);
    }

    /**
     * Creates an InvokerWithNotification.
     *
     * @param portTypeQName
     * @param wsdlLocation  The URL of WSDL of the service to invoke
     * @param nodeID        The ID of the service
     * @param gfacURL       The URL of GFac service.
     * @param notifier      The notification sender
     */
    public EmbeddedGFacInvoker(QName portTypeQName, String wsdlLocation, String nodeID, String gfacURL,
                               WorkflowNotifiable notifier) {
        this(portTypeQName, wsdlLocation, nodeID, null, gfacURL, notifier);
    }

    /**
     * Creates an InvokerWithNotification.
     *
     * @param portTypeQName
     * @param wsdlLocation  The URL of WSDL of the service to invoke
     * @param nodeID        The ID of the service
     * @param messageBoxURL
     * @param gfacURL       The URL of GFac service.
     * @param notifier      The notification sender
     */
    public EmbeddedGFacInvoker(QName portTypeQName, String wsdlLocation, String nodeID, String messageBoxURL,
                               String gfacURL, WorkflowNotifiable notifier) {
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.wsdlLocation = wsdlLocation;
        this.serviceInformation = wsdlLocation;
        this.messageBoxURL = messageBoxURL;
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);

        this.failerSent = false;
    }

    /**
     * @param portTypeQName
     * @param wsdl
     * @param nodeID
     * @param messageBoxURL
     * @param gfacURL
     * @param notifier
     */
    public EmbeddedGFacInvoker(QName portTypeQName,
                               WsdlDefinitions wsdl,
                               String nodeID,
                               String messageBoxURL,
                               String gfacURL,
                               WorkflowNotifiable notifier,
                               String topic,
                               AiravataAPI airavataAPI,
                               String serviceName,
                               XBayaConfiguration config) {
        final String wsdlStr = xsul.XmlConstants.BUILDER.serializeToString(wsdl);
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.wsdlDefinitionObject = wsdl;
        this.messageBoxURL = messageBoxURL;
        this.serviceInformation = wsdlStr;
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);
        this.airavataAPI = airavataAPI;
        this.topic = topic;
        this.serviceName = serviceName;
        this.failerSent = false;
        this.configuration = config;
    }

    /**
     * @throws WorkflowException
     */
    public void setup() throws WorkflowException {
        this.notifier.setServiceID(this.nodeID);
    }

    private void setup(WsdlDefinitions definitions) throws WorkflowException {
    }

    /**
     * @param operationName The name of the operation
     * @throws WorkflowException
     */
    public void setOperation(String operationName) throws WorkflowException {
    }

    /**
     * @param name  The name of the input parameter
     * @param value The value of the input parameter
     * @throws WorkflowException
     */
    public void setInput(String name, Object value) throws WorkflowException {
        try {
            if (value instanceof XmlElement) {
                logger.debug("value: " + XMLUtil.xmlElementToString((XmlElement) value));
            }
            this.inputNames.add(name);
            this.inputValues.add(value);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error in setting an input. name: " + name + " value: " + value;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * @return
     * @throws WorkflowException
     */
    public synchronized boolean invoke() throws WorkflowException {
        try {
            ContextHeaderDocument.ContextHeader contextHeader =
                    WorkflowContextHeaderBuilder.removeOtherSchedulingConfig(nodeID, this.configuration.getContextHeader());
            String hostName = null;
            HostDescription registeredHost;
            if (contextHeader != null) {
                if (contextHeader.getWorkflowSchedulingContext() != null &&
                        contextHeader.getWorkflowSchedulingContext().getApplicationSchedulingContextArray().length > 0 &&
                        contextHeader.getWorkflowSchedulingContext().getApplicationSchedulingContextArray(0).getHostName() != null) {
                    hostName = contextHeader.getWorkflowSchedulingContext().getApplicationSchedulingContextArray(0).getHostName();
                }
            }
            //todo This is the basic scheduling, have to do proper scheduling implementation by implementing HostScheduler interface
            ServiceDescription serviceDescription = airavataAPI.getApplicationManager().getServiceDescription(serviceName);
            if (hostName == null) {
                List<HostDescription> registeredHosts = new ArrayList<HostDescription>();
                Map<String, ApplicationDescription> applicationDescriptors = airavataAPI.getApplicationManager().getApplicationDescriptors(serviceName);
                for (String hostDescName : applicationDescriptors.keySet()) {
                    registeredHosts.add(airavataAPI.getApplicationManager().getHostDescription(hostDescName));
                }
                Class<? extends HostScheduler> aClass = Class.forName(ServerSettings.getHostScheduler()).asSubclass(HostScheduler.class);
                HostScheduler hostScheduler = aClass.newInstance();
                registeredHost = hostScheduler.schedule(registeredHosts);
            } else {
                // if user specify a host, no matter what we pick that host for all the nodes, todo: allow users to specify node specific host
                registeredHost = airavataAPI.getApplicationManager().getHostDescription(hostName);
            }
            ApplicationDescription applicationDescription =
                    airavataAPI.getApplicationManager().getApplicationDescription(serviceName, registeredHost.getType().getHostName());

            // When we run getInParameters we set the actualParameter object, this has to be fixed
            URL resource = EmbeddedGFacInvoker.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
            OMElement inputMessage = getInParameters();
            Object wsifMessageElement = new WSIFMessageElement(XMLUtil.stringToXmlElement3(inputMessage.toStringWithConsume()));
            this.notifier.invokingService(new WSIFMessageElement((XmlElement) wsifMessageElement));
            Properties configurationProperties = ServerSettings.getProperties();
            GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), airavataAPI, configurationProperties);

            JobExecutionContext jobExecutionContext = new JobExecutionContext(gFacConfiguration, serviceName);
            //Here we get only the contextheader information sent specific for this node
            //Add security context

            //FIXME - We no longer using job execution context
//            jobExecutionContext.setContextHeader(WorkflowContextHeaderBuilder.removeOtherSchedulingConfig(nodeID, configuration.getContextHeader()));


            jobExecutionContext.setProperty(Constants.PROP_WORKFLOW_NODE_ID, this.nodeID);
            jobExecutionContext.setProperty(Constants.PROP_TOPIC, this.configuration.getTopic());
            jobExecutionContext.setProperty(Constants.PROP_BROKER_URL, this.configuration.getBrokerURL().toASCIIString());
            jobExecutionContext.setProperty(Constants.PROP_WORKFLOW_INSTANCE_ID, this.configuration.getTopic());


            ApplicationContext applicationContext = new ApplicationContext();
            applicationContext.setApplicationDeploymentDescription(applicationDescription);
            applicationContext.setHostDescription(registeredHost);
            applicationContext.setServiceDescription(serviceDescription);

            jobExecutionContext.setApplicationContext(applicationContext);

            jobExecutionContext.setOutMessageContext(getOutParameters(serviceDescription));
            jobExecutionContext.setInMessageContext(new MessageContext(actualParameters));

            addSecurityContext(registeredHost, configurationProperties, jobExecutionContext,
                    configuration.getContextHeader());
            GFacImpl gfacAPI1 = new GFacImpl();
            gfacAPI1.submitJob(jobExecutionContext);

            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2/xsd", "ns1");
            OMElement outputElement = fac.createOMElement("invokeResponse", omNs);
            MessageContext outMessageContext = jobExecutionContext.getOutMessageContext();
            Set<String> paramNames = outMessageContext.getParameters().keySet();
            for (String paramName : paramNames) {
                /*
                * Process Output
                */
                String outputString = ((ActualParameter) outMessageContext.getParameter(paramName)).toXML().replaceAll("GFacParameter", paramName);
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(outputString));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                outputElement.addChild(builder.getDocumentElement());
            }
            // Send notification
            logger.debug("outputMessage: " + outputElement.toString());
            outPut = new WSIFMessageElement(XMLUtil.stringToXmlElement3(outputElement.toStringWithConsume()));
            this.result = true;
            EmbeddedGFacInvoker.this.notifier.serviceFinished(new WSIFMessageElement((XmlElement) outPut));
            //todo check whether ActualParameter values are set or not, if they are null have to through an error or handle this in gfac level.
//             {
//                // An implementation of WSIFMessage,
//                // WSIFMessageElement, implements toString(), which
//                // serialize the message XML.
//                EmbeddedGFacInvoker.this.notifier.receivedFault(new WSIFMessageElement(XMLUtil.stringToXmlElement3("<Message>Invocation Failed</Message>")));
//                EmbeddedGFacInvoker.this.failerSent = true;
//            }

        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error in invoking a service: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Exception e) {
            this.notifier.invocationFailed(e.getMessage(), e);
            throw new WorkflowException(e.getMessage(), e);
        }
        return true;
    }

    private SecurityContextDocument.SecurityContext.CredentialManagementService getCredentialManagementService(
            ContextHeaderDocument.ContextHeader contextHeader) {

        if (contextHeader != null) {

            SecurityContextDocument.SecurityContext.CredentialManagementService credentialManagementService
                    = contextHeader.getSecurityContext().getCredentialManagementService();

            if (credentialManagementService != null) {
                // Make sure token id and portal user id is properly populated
                if (credentialManagementService.getTokenId() != null &&
                        credentialManagementService.getPortalUser() != null) {

                    return credentialManagementService;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        return null;
    }

    private void addSecurityContext(HostDescription registeredHost, Properties configurationProperties,
                                    JobExecutionContext jobExecutionContext, ContextHeaderDocument.ContextHeader contextHeader) throws WorkflowException {
        RequestData requestData;
            /* todo fix the credential store and uncomment following code block
            SecurityContextDocument.SecurityContext.CredentialManagementService credentialManagementService
                    = getCredentialManagementService(contextHeader);

            GSISecurityContext context;


            if (credentialManagementService != null) {
                String gatewayId = credentialManagementService.getGatewayId();
                String tokenId
                        = credentialManagementService.getTokenId();
                String portalUser = credentialManagementService.getPortalUser();

                requestData = new RequestData(tokenId, portalUser, gatewayId);
            } else {
                requestData = new RequestData("default");
            }

            try {
                context = new GSISecurityContext(CredentialReaderFactory.createCredentialStoreReader(), requestData);
            } catch (Exception e) {
                throw new WorkflowException("An error occurred while creating GSI security context", e);
            }
            if (registeredHost.getType() instanceof GsisshHostType) {
                GSIAuthenticationInfo authenticationInfo
                        = new MyProxyAuthenticationInfo(requestData.getMyProxyUserName(), requestData.getMyProxyPassword(), requestData.getMyProxyServerUrl(),
                        requestData.getMyProxyPort(), requestData.getMyProxyLifeTime(), System.getProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY));
                ServerInfo serverInfo = new ServerInfo(requestData.getMyProxyUserName(), registeredHost.getType().getHostAddress());

                Cluster pbsCluster = null;
                try {
                    pbsCluster = new PBSCluster(serverInfo, authenticationInfo,
                            (((HpcApplicationDeploymentType) jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath()));
                } catch (SSHApiException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                context.setPbsCluster(pbsCluster);
            }
            */
            requestData = new RequestData("default");
            GSISecurityContext context;
            try {
                context = new GSISecurityContext(CredentialReaderFactory.createCredentialStoreReader(), requestData);
            } catch (Exception e) {
                throw new WorkflowException("An error occurred while creating GSI security context", e);
            }

            if (registeredHost.getType() instanceof GsisshHostType) {
                GSIAuthenticationInfo authenticationInfo
                        = new MyProxyAuthenticationInfo(requestData.getMyProxyUserName(), requestData.getMyProxyPassword(), requestData.getMyProxyServerUrl(),
                        requestData.getMyProxyPort(), requestData.getMyProxyLifeTime(), System.getProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY));
                ServerInfo serverInfo = new ServerInfo(requestData.getMyProxyUserName(), registeredHost.getType().getHostAddress());

                Cluster pbsCluster = null;
                try {
                    pbsCluster = new PBSCluster(serverInfo, authenticationInfo,
                            (((HpcApplicationDeploymentType) jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath()));
                } catch (SSHApiException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                context.setPbsCluster(pbsCluster);
            }

            jobExecutionContext.addSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT, context);
        //Adding Amanzon Keys
            if (this.configuration.getAmazonSecurityContext() != null) {
                jobExecutionContext.addSecurityContext(AmazonSecurityContext.AMAZON_SECURITY_CONTEXT,
                        this.configuration.getAmazonSecurityContext());
         }
      //Adding SSH security
            String sshUserName = configurationProperties.getProperty(Constants.SSH_USER_NAME);
            String sshPrivateKey = configurationProperties.getProperty(Constants.SSH_PRIVATE_KEY);
            String sshPrivateKeyPass = configurationProperties.getProperty(Constants.SSH_PRIVATE_KEY_PASS);
            String sshPassword = configurationProperties.getProperty(Constants.SSH_PASSWORD);
            String sshPublicKey = configurationProperties.getProperty(Constants.SSH_PUBLIC_KEY);
            SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
            if (((SSHHostType) registeredHost.getType()).getHpcResource()) {
                AuthenticationInfo authenticationInfo = null;
                // we give higher preference to the password over keypair ssh authentication
                if (sshPassword != null) {
                    authenticationInfo = new DefaultPasswordAuthenticationInfo(sshPassword);
                } else {
                    authenticationInfo = new DefaultPublicKeyFileAuthentication(sshPublicKey, sshPrivateKey, sshPrivateKeyPass);
                }
                ServerInfo serverInfo = new ServerInfo(sshUserName, registeredHost.getType().getHostAddress());

                Cluster pbsCluster = null;
                try {
                    pbsCluster = new PBSCluster(serverInfo, authenticationInfo,
                            (((HpcApplicationDeploymentType) jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath()));
                } catch (SSHApiException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                sshSecurityContext.setPbsCluster(pbsCluster);
                sshSecurityContext.setUsername(sshUserName);
            } else {
                sshSecurityContext = new SSHSecurityContext();
                sshSecurityContext.setUsername(sshUserName);
                sshSecurityContext.setPrivateKeyLoc(sshPrivateKey);
                sshSecurityContext.setKeyPass(sshPrivateKeyPass);
            }
            jobExecutionContext.addSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT, sshSecurityContext);
    }

    /**
     * @throws WorkflowException
     */
    @SuppressWarnings("boxing")
    public synchronized void waitToFinish() throws WorkflowException {
        try {
            while (this.result == null) {
                // The job is not submitted yet.
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            // Wait for the job to finish.
            Boolean success = this.result;
            if (success == false) {
                WSIFMessage faultMessage = this.invoker.getFault();
                String message = "Error in a service: ";
                // An implementation of WSIFMessage,
                // WSIFMessageElement, implements toString(), which
                // serialize the message XML.
                message += faultMessage.toString();
                throw new WorkflowException(message);
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error while waiting for a service to finish: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        }
    }

    /**
     * @param name The name of the output parameter
     * @return
     * @throws WorkflowException
     */
    public Object getOutput(String name) throws WorkflowException {
        try {
            waitToFinish();
            if (outPut instanceof XmlElement) {
                Iterator children = ((XmlElement) outPut).children();
                while (children.hasNext()) {
                    Object next = children.next();
                    if (((XmlElement) next).getName().equals(name)) {
                        return ((XmlElement) ((XmlElement) next).children().next()).children().next();
                    }
                }
            } else {
                return outPut;
            }
        } catch (WorkflowException e) {
            logger.error(e.getMessage(), e);
            // An appropriate message has been set in the exception.
            if (!this.failerSent) {
                this.notifier.invocationFailed(e.getMessage(), e);
            }
            throw e;
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error while waiting for a output: " + name;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        }
        throw new WorkflowException("Output could not be found");
    }

    /**
     * @return
     * @throws WorkflowException
     */
    public WSIFMessage getOutputs() throws WorkflowException {
        return this.invoker.getOutputs();
    }

    public WSIFClient getClient() {
        return null;
    }

    public WSIFMessage getInputs() throws WorkflowException {
        return null;
    }

    public WSIFMessage getFault() throws WorkflowException {
        return null;
    }

    private OMElement getInParameters() throws AiravataAPIInvocationException, RegistryException, XMLStreamException {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement invoke_inputParams = omFactory.createOMElement(new QName("invoke_InputParams"));
        ServiceDescription serviceDescription = airavataAPI.getApplicationManager().getServiceDescription(this.serviceName);
        if (serviceDescription == null) {
            throw new RegistryException(new Exception("Service Description not found in registry."));
        }
        ServiceDescriptionType serviceDescriptionType = serviceDescription.getType();
        for (String inputName : this.inputNames) {
            OMElement omElement = omFactory.createOMElement(new QName(inputName));
            int index = this.inputNames.indexOf(inputName);
            Object value = this.inputValues.get(index);
            InputParameterType parameter = serviceDescriptionType.getInputParametersArray(index);
            if (value instanceof XmlElement) {
                omElement.setText((String) ((XmlElement) ((XmlElement) ((XmlElement) value).children().next()).children().next()).children().next());
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(XMLUtil.xmlElementToString((XmlElement) value)));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement input = builder.getDocumentElement();
//                actualParameters.put(parameter.getParameterName(), GFacUtils.getInputActualParameter(parameter, input));
            } else if (value instanceof String) {
                omElement.setText((String) value);
//                actualParameters.put(parameter.getParameterName(), GFacUtils.getInputActualParameter(parameter, AXIOMUtil.stringToOM("<value>" + value + "</value>")));
            }
            invoke_inputParams.addChild(omElement);
        }
        return invoke_inputParams;
    }

    private MessageContext getOutParameters(ServiceDescription serviceDescription) {
        MessageContext outContext = new MessageContext();
        for (OutputParameterType parameter : serviceDescription.getType().getOutputParametersArray()) {
            ActualParameter actualParameter = new ActualParameter();
            if ("String".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(StringParameterType.type);
            } else if ("Double".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(DoubleParameterType.type);
            } else if ("Integer".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(IntegerParameterType.type);
            } else if ("Float".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(FloatParameterType.type);
            } else if ("Boolean".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(BooleanParameterType.type);
            } else if ("File".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(FileParameterType.type);
            } else if ("URI".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(URIParameterType.type);
            } else if ("StringArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(StringArrayType.type);
            } else if ("DoubleArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(DoubleArrayType.type);
            } else if ("IntegerArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(IntegerArrayType.type);
            } else if ("FloatArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(FloatArrayType.type);
            } else if ("BooleanArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(BooleanArrayType.type);
            } else if ("FileArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(FileArrayType.type);
            } else if ("URIArray".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(URIArrayType.type);
            } else if ("StdOut".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(StdOutParameterType.type);
            } else if ("StdErr".equals(parameter.getParameterType().getName())) {
                actualParameter.getType().changeType(StdErrParameterType.type);
            }
            outContext.addParameter(parameter.getParameterName(), actualParameter);
        }
        return outContext;
    }
}
