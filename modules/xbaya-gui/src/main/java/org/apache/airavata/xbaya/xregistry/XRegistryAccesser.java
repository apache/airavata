/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
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

package org.apache.airavata.xbaya.xregistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.experiment.gui.OGCEXRegistryWorkflowPublisherWindow;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyDialog;
import org.apache.airavata.xbaya.security.SecurityUtil;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.util.StringUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.util.XmlFormatter;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.xmlbeans.XmlException;
import org.ietf.jgss.GSSCredential;
import org.ogce.schemas.gfac.beans.ApplicationBean;
import org.ogce.schemas.gfac.beans.HostBean;
import org.ogce.schemas.gfac.beans.ServiceBean;
import org.ogce.schemas.gfac.beans.utils.ApplicationUtils;
import org.ogce.schemas.gfac.beans.utils.GFacSchemaException;
import org.ogce.schemas.gfac.beans.utils.HostUtils;
import org.ogce.schemas.gfac.beans.utils.ServiceUtils;
import org.ogce.xregistry.client.XRegistryClient;
import org.ogce.xregistry.utils.XRegClientConstants;
import org.ogce.xregistry.utils.XRegistryClientException;
import org.xmlpull.infoset.XmlElement;

import xregistry.generated.FindAppDescResponseDocument.FindAppDescResponse.AppData;
import xregistry.generated.HostDescData;
import xregistry.generated.OGCEResourceData;
import xregistry.generated.ResourceData;
import xregistry.generated.ServiceDescData;

public class XRegistryAccesser {

    /**
     * PUBLIC_ACTOR
     */
    public static final String PUBLIC_ACTOR = "public";

    private XBayaEngine engine;

    private XRegistryClient xregistryClient;

    private GSSCredential gssCredential;

    private URI xregistryURL;

    /**
     * Constructs a XRegistryAccesser.
     * 
     * @param engine
     */
    public XRegistryAccesser(XBayaEngine engine) {
        this.engine = engine;
        this.gssCredential = this.engine.getMyProxyClient().getProxy();
        this.xregistryURL = this.engine.getConfiguration().getXRegistryURL();
    }

    /**
     * Constructs a XRegistryAccesser.
     * 
     * @param engine
     */
    public XRegistryAccesser(GSSCredential gssCredential, URI xRegistryURL) {
        this.gssCredential = gssCredential;
        this.xregistryURL = xRegistryURL;
    }

    /**
     * Constructs a XRegistryAccesser.
     * 
     * @param engine
     */
    public XRegistryAccesser(String userName, String password, String myproxyServer, URI xRegistryURL) {

        this.gssCredential = SecurityUtil.getGSSCredential(userName, password, myproxyServer);
        this.xregistryURL = xRegistryURL;
    }

    /**
     * Connect to XRegistry Service, the constructors should have filled the credential and service url
     * 
     * @return xregistryconnection
     * @throws XregistryException
     */
    private void connectToXRegistry() throws XRegistryClientException {
        if (null == this.gssCredential) {
            new MyProxyDialog(this.engine).show(true);
            this.gssCredential = this.engine.getMyProxyClient().getProxy();
            // if its still null => user cancelled
            if (null == this.gssCredential) {
                throw new XBayaRuntimeException("GSI Credintial cannot be null");
            }
        }
        if (this.xregistryURL == null) {
            this.xregistryURL = XBayaConstants.DEFAULT_XREGISTRY_URL;
        }
        if (this.xregistryURL == null) {
            throw new XBayaRuntimeException("XRegistry URL cannot be null");
        }

        try {
            this.xregistryClient = new XRegistryClient(this.gssCredential, XBayaSecurity.getTrustedCertificates(),
                    this.xregistryURL.toString());
        } catch (XRegistryClientException e) {
            throw new XBayaRuntimeException(e);
        }
    }

    private XRegistryClientException convertXRegistryClientException(Throwable th) {
        String message = th.getMessage();
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new StringReader(message));
            while ((line = br.readLine()) != null) {
                if (!line.contains("xregistry.XregistryException"))
                    continue;

                String[] split = line.split(":", 3);
                return new XRegistryClientException(split[2], th);
            }
            return new XRegistryClientException(th);
        } catch (Exception e) {
            // no op
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ex) {
                    // no op
                }
            }
        }
        return null;
    }

    /**
     * Fetch all workflow templates from XRegistry
     * 
     * @return workflow templates
     * @throws XregistryException
     */
    public Map<QName, OGCEResourceData> getOGCEWorkflowTemplateList() throws XRegistryClientException {
        if (this.xregistryClient == null) {
            connectToXRegistry();
        }
        Map<QName, OGCEResourceData> val = new HashMap<QName, OGCEResourceData>();
        OGCEResourceData[] resources = this.xregistryClient.findOGCEResource("",
                XRegClientConstants.ResourceType.WorkflowTemplate.toString(), null);
        for (OGCEResourceData resource : resources) {
            val.put(resource.getResourceID(), resource);
        }
        return val;
    }

    /**
     * @param hostName
     * @return host templates
     * @throws XRegistryClientException
     */
    public Map<QName, HostDescData> getHostDescByName(String hostName) throws XRegistryClientException {
        if (this.xregistryClient == null) {
            connectToXRegistry();
        }
        Map<QName, HostDescData> val = new HashMap<QName, HostDescData>();
        HostDescData[] hostDescs = this.searchHostByName(hostName);
        for (HostDescData hostDesc : hostDescs) {
            val.put(hostDesc.getName(), hostDesc);
        }

        return val;
    }

    /**
     * @param appName
     * @return application templates
     * @throws XRegistryClientException
     */
    public Map<QName, AppData> getApplicationDescByName(String appName) throws XRegistryClientException {
        if (this.xregistryClient == null) {
            connectToXRegistry();
        }
        Map<QName, AppData> val = new HashMap<QName, AppData>();
        AppData[] appDescs = this.searchApplicationByName(appName);
        for (AppData appDesc : appDescs) {
            val.put(appDesc.getName(), appDesc);
        }

        return val;
    }

    /**
     * @param serviceName
     * @return service templates
     * @throws XRegistryClientException
     */
    public Map<QName, ServiceDescData> getServiceDescByName(String serviceName) throws XRegistryClientException {
        if (this.xregistryClient == null) {
            connectToXRegistry();
        }
        Map<QName, ServiceDescData> val = new HashMap<QName, ServiceDescData>();
        ServiceDescData[] serviceDescs = this.searchServiceByName(serviceName);
        for (ServiceDescData serviceDesc : serviceDescs) {
            val.put(serviceDesc.getName(), serviceDesc);
        }

        return val;
    }

    /**
     * Retrieves workflow from xregistry.
     * 
     * @param workflowTemplateId
     * @return workflow
     * @throws XRegistryClientException
     * @throws XregistryException
     * @throws GraphException
     * @throws ComponentException
     * @throws Exception
     */
    public Workflow getOGCEWorkflow(QName workflowTemplateId) throws XRegistryClientException, GraphException,
            ComponentException, Exception {
        if (this.xregistryClient == null) {
            connectToXRegistry();
        }
        String workflowString = this.xregistryClient.getOGCEResource(workflowTemplateId,
                XRegClientConstants.ResourceType.WorkflowTemplate.toString(), null);

        XmlElement xwf = XMLUtil.stringToXmlElement(workflowString);
        Workflow workflow = new Workflow(xwf);
        return workflow;
    }

    /**
     * Save the current workflow to the XRegistry
     */
    public void saveWorkflow() {

        boolean makePublic = false;

        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            Workflow workflow = this.engine.getWorkflow();
            JythonScript script = new JythonScript(workflow, this.engine.getConfiguration());

            // Check if there is any errors in the workflow first.
            ArrayList<String> warnings = new ArrayList<String>();
            if (!script.validate(warnings)) {
                StringBuilder buf = new StringBuilder();
                for (String warning : warnings) {
                    buf.append("- ");
                    buf.append(warning);
                    buf.append("\n");
                }
                this.engine.getErrorWindow().warning(buf.toString());
                return;
            }
            OGCEXRegistryWorkflowPublisherWindow registryPublishingWindow = new OGCEXRegistryWorkflowPublisherWindow(
                    this.engine);
            registryPublishingWindow.show();

            String workflowId = workflow.getName();

            workflowId = StringUtil.convertToJavaIdentifier(workflowId);

            // FIXME::Commenting the workflow UUID. It is debatable if the
            // workflow template id should be unique or not.
            // workflowId = workflowId + UUID.randomUUID();

            QName workflowQName = new QName(XBayaConstants.OGCE_WORKFLOW_NS, workflowId);

            // first find whether this resource is already in xregistry
            // TODO: Add the check back
            // DocData[] resource =
            // client.findOGCEResource(workflowQName.toString(), "Workflow",
            // null);
            // if (resource != null && !"".equals(resource)) {
            // // if already there then remove
            //
            // int result =
            // JOptionPane.showConfirmDialog(this.engine.getGUI().getGraphCanvas().getSwingComponent(),
            // "Workflow Already Exist in Xregistry. Do you want to overwrite",
            // "Workflow already exist", JOptionPane.YES_NO_OPTION);
            // if(result != JOptionPane.YES_OPTION){
            // return;
            // }
            // client.removeResource(workflowQName);
            // }
            String workflowAsString = XMLUtil.xmlElementToString(workflow.toXML());
            String owner = this.engine.getMyProxyClient().getProxy().getName().toString();

            this.xregistryClient.registerOGCEResource(workflowQName, workflow.getName(),
                    XBayaConstants.XR_Resource_Types.WorkflowTemplate.toString(), workflow.getDescription(),
                    workflowAsString, null, owner);

            makePublic = registryPublishingWindow.isMakePublic();
            if (makePublic) {
                this.xregistryClient.addCapability(workflowQName.toString(), PUBLIC_ACTOR, false,
                        XRegClientConstants.Action.Read.toString());
            }
            registryPublishingWindow.hide();

        } catch (Exception e) {
            this.engine.getErrorWindow().error(e.getMessage(), e);
        }
    }

    /**
     * Deletes a workflow from the XRegistry given the resource QName
     * 
     * @param workflowTemplateId
     * @param xRegistryURL
     * @param context
     * @throws XRegistryClientException
     * @throws XregistryException
     */
    public void deleteOGCEWorkflow(QName workflowTemplateId) throws XRegistryClientException {
        if (this.xregistryClient == null) {
            connectToXRegistry();
        }
        this.xregistryClient.removeOGCEResource(workflowTemplateId,
                XBayaConstants.XR_Resource_Types.WorkflowTemplate.toString());
    }

    /**
     * @param hostName
     * @throws XRegistryClientException
     */
    public void deleteHostDescription(String hostName) throws XRegistryClientException {
        if (this.xregistryClient == null) {
            connectToXRegistry();
        }

        this.xregistryClient.removeHostDesc(hostName);
    }

    /**
     * @param appName
     * @param hostName
     * @throws XRegistryClientException
     */
    public void deleteAppDescription(QName appName, String hostName) throws XRegistryClientException {
        if (this.xregistryClient == null) {
            connectToXRegistry();
        }

        this.xregistryClient.removeAppDesc(appName, hostName);
    }

    /**
     * @param serviceName
     * @throws XRegistryClientException
     */
    public void deleteServiceDescrption(QName serviceName) throws XRegistryClientException {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            this.xregistryClient.removeServiceDesc(serviceName);
        } catch (Exception e) {
            throw convertXRegistryClientException(e);
        }
    }

    /**
     * Returns a Workflow object with the Template IDs properly set and ready to be loaded into XBaya
     * 
     * @param xRegistryURI
     * @param gssCredential
     *            Proxy credential
     * @param qname
     *            Qname of the Workflow that is used to store in XRegistry
     * @return Workflow
     */
    public Workflow getWorkflow(QName qname) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            String resource = this.xregistryClient.getResource(qname);
            XmlElement xwf = XMLUtil.stringToXmlElement(resource);
            Workflow workflow = new Workflow(xwf);
            return workflow;
        } catch (Exception e) {
            throw new XBayaRuntimeException(e);
        }
    }

    /**
     * 
     * @return XRegistryClient
     */
    public XRegistryClient getXRegistryClient() {
        return this.xregistryClient;

    }

    /**
     * @param hostName
     * @return HostDescData Array
     */
    public HostDescData[] searchHostByName(String hostName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            return this.xregistryClient.findHosts(hostName);
        } catch (XRegistryClientException e) {
            throw new XBayaRuntimeException(e);
        }
    }

    /**
     * @param appName
     * @return AppData Array
     */
    public AppData[] searchApplicationByName(String appName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            return this.xregistryClient.findAppDesc(appName);
        } catch (XRegistryClientException e) {
            throw new XBayaRuntimeException(e);
        }
    }

    /**
     * @param serviceName
     * @return ServiceData Array
     */
    public ServiceDescData[] searchServiceByName(String serviceName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            return this.xregistryClient.findServiceDesc(serviceName);
        } catch (XRegistryClientException e) {
            throw new XBayaRuntimeException(e);
        }
    }

    /**
     * @param rescName
     * @return ResourceData Array
     */
    public ResourceData[] searchResourceByName(String rescName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            return this.xregistryClient.findResource(rescName);
        } catch (XRegistryClientException e) {
            throw new XBayaRuntimeException(e);
        }
    }

    /**
     * @param hostBean
     * @return Registration Result
     */
    public boolean registerHost(HostBean hostBean) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            String toFormatXML = HostUtils.simpleHostXMLRequest(hostBean);
            String formattedXML = XmlFormatter.format(toFormatXML);


            this.xregistryClient.registerHostDesc(formattedXML);

            return true;
        } catch (XRegistryClientException e) {
            e.printStackTrace();
            return false;
        } catch (GFacSchemaException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param appBean
     * @return Registration Result
     */
    public boolean registerApplication(ApplicationBean appBean) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            String toFormatXML = ApplicationUtils.simpleAppXMLRequest(appBean);
            String formattedXML = XmlFormatter.format(toFormatXML);


            this.xregistryClient.registerAppDesc(formattedXML);

            return true;
        } catch (XRegistryClientException e) {
            return false;
        }
    }

    /**
     * @param serviceBean
     * @return Registration Result
     */
    public boolean registerService(ServiceBean serviceBean) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            String toFormatXML = ServiceUtils.simpleServiceXMLRequest(serviceBean);
            String formattedXML = XmlFormatter.format(toFormatXML);
            String serviceWsdl = ServiceUtils.createAwsdl4ServiceMap(formattedXML);


            this.xregistryClient.registerServiceDesc(formattedXML, serviceWsdl);

            return true;
        } catch (XRegistryClientException e) {
            e.printStackTrace();
            return false;
        } catch (GFacSchemaException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param hostDesc
     * @return Registration Result
     */
    public boolean registerHost(String hostDesc) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            this.xregistryClient.registerHostDesc(hostDesc);
            return true;
        } catch (XRegistryClientException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param appDesc
     * @return Registration Result
     */
    public boolean registerApplication(String appDesc) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            this.xregistryClient.registerAppDesc(appDesc);
            return true;
        } catch (XRegistryClientException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param serviceDesc
     * @return Registration Result
     */
    public boolean registerService(String serviceDesc) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }

            String awsdlAsStr = ServiceUtils.createAwsdl4ServiceMap(serviceDesc);
            this.xregistryClient.registerServiceDesc(serviceDesc, awsdlAsStr);
            return true;
        } catch (XRegistryClientException e) {
            e.printStackTrace();
            return false;
        } catch (GFacSchemaException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param hostName
     * @return Host Description
     */
    public HostBean getHostBean(String hostName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            String hostDesc = this.xregistryClient.getHostDesc(hostName);
            return HostUtils.simpleHostBeanRequest(hostDesc);
        } catch (XRegistryClientException e) {
            e.printStackTrace();
            return null;
        } catch (XmlException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param hostName
     * @return Host Description
     */
    public String getHostDesc(String hostName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            String hostDesc = this.xregistryClient.getHostDesc(hostName);
            return hostDesc;
        } catch (XRegistryClientException e) {
            return e.getMessage();
        }
    }

    /**
     * @param appName
     * @param hostName
     * @return ApplicationBean
     */
    public ApplicationBean getApplicationBean(String appName, String hostName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            String appDesc = this.xregistryClient.getAppDesc(appName, hostName);
            return ApplicationUtils.simpleApplicationBeanRequest(appDesc);
        } catch (XRegistryClientException e) {
            e.printStackTrace();
            return null;
        } catch (XmlException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param appName
     * @param hostName
     * @return Application Description
     */
    public String getApplicationDesc(String appName, String hostName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            String appDesc = this.xregistryClient.getAppDesc(appName, hostName);
            return appDesc;
        } catch (XRegistryClientException e) {
            return e.getMessage();
        }
    }

    /**
     * @param serviceName
     * @return ServiceBean
     */
    public ServiceBean getServiceBean(QName serviceName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            String serviceMapStr = this.xregistryClient.getServiceDesc(serviceName);
            return ServiceUtils.serviceBeanRequest(serviceMapStr);
        } catch (XRegistryClientException e) {
            e.printStackTrace();
            return null;
        } catch (XmlException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param serviceName
     * @return Service Description
     */
    public String getServiceDesc(QName serviceName) {
        try {
            if (this.xregistryClient == null) {
                connectToXRegistry();
            }
            String serviceMapStr = this.xregistryClient.getServiceDesc(serviceName);
            return serviceMapStr;
        } catch (XRegistryClientException e) {
            return e.getMessage();
        }
    }

    // following method are now deprecated. Now all workflows should be savaed
    // as ogecresources with workflowtemplate as types.

    /**
     * Retrieves all documents from XRegistry
     * 
     * @return
     * @throws XRegistryClientException
     * @throws XregistryException
     */
    // public Map<QName, ResourceData> list() throws XRegistryClientException {
    // if (this.xregistryClient == null) {
    // connectToXRegistry();
    // }
    // Map<QName, ResourceData> val = new HashMap<QName, ResourceData>();
    // ResourceData[] resources = this.xregistryClient.findResource("");
    // for (ResourceData resource : resources) {
    // QName resourceName = resource.getName();
    // if (resourceName != null
    // && XBayaConstants.LEAD_NS.equals(resourceName
    // .getNamespaceURI()))
    // val.put(resourceName, resource);
    // }
    // return val;
    // }

    /**
     * Export the workflow to the X-Registry
     */
    // public void exportWorkflow() {
    //
    // boolean makePublic = false;
    //
    // try {
    // if (this.xregistryClient == null) {
    // connectToXRegistry();
    // }
    // Workflow workflow = this.engine.getWorkflow();
    // JythonScript script = new JythonScript(workflow, this.engine
    // .getConfiguration());
    //
    // // Check if there is any errors in the workflow first.
    // ArrayList<String> warnings = new ArrayList<String>();
    // if (!script.validate(warnings)) {
    // StringBuilder buf = new StringBuilder();
    // for (String warning : warnings) {
    // buf.append("- ");
    // buf.append(warning);
    // buf.append("\n");
    // }
    // this.engine.getErrorWindow().warning(buf.toString());
    // return;
    // }
    //
    // XRegistryWorkflowPublisherWindow registryPublishingWindow = new
    // XRegistryWorkflowPublisherWindow(
    // this.engine);
    // registryPublishingWindow.show();
    // String finalWorkflowName = registryPublishingWindow.getLabel();
    // if (null != finalWorkflowName && !"".equals(finalWorkflowName)) {
    // workflow.setName(finalWorkflowName);
    // }
    //
    // // first find whether this resource is already in xregistry
    // String resource = this.xregistryClient.getResource(workflow
    // .getQname());
    // if (resource != null && !"".equals(resource)) {
    // // if already there then remove
    // int result = JOptionPane
    // .showConfirmDialog(
    // this.engine.getGUI().getGraphCanvas()
    // .getSwingComponent(),
    // "Workflow Already Exist in Xregistry. Do you want to overwrite",
    // "Workflow already exist",
    // JOptionPane.YES_NO_OPTION);
    // if (result != JOptionPane.YES_OPTION) {
    // return;
    // }
    // this.xregistryClient.removeResource(workflow.getQname());
    // }
    // String workflowAsString = XMLUtil.xmlElementToString(workflow
    // .toXML());
    // this.xregistryClient.registerResource(workflow.getQname(),
    // workflowAsString);
    // makePublic = registryPublishingWindow.isMakePublic();
    // if (makePublic) {
    // this.xregistryClient.addCapability(workflow.getQname()
    // .toString(), PUBLIC_ACTOR, false,
    // XRegClientConstants.Action.Read.toString());
    // }
    // registryPublishingWindow.hide();
    //
    // } catch (Exception e) {
    // this.engine.getErrorWindow().error(e.getMessage(), e);
    // }
    // }

    /**
     * Returns all the workflows that the user associated with the credential has access to
     * 
     * @param xRegistryURL
     *            Xregistry URL eg:https://silktree.cs.indiana.edu:6666/xregistry
     * @param credential
     *            Proxy credential
     * @return List of workflows
     */
    // public Workflow[] getWorkflows() {
    // return getWorkflows(-1);
    // }

    /**
     * Returns all the workflows that the user associated with the credential has access to
     * 
     * @param xRegistryURL
     *            Xregistry URL eg:https://silktree.cs.indiana.edu:6666/xregistry
     * @param credential
     *            Proxy credential
     * @param resultSetSize
     *            return array will be limited to this
     * @return List of workflows
     */
    // public Workflow[] getWorkflows(int resultSetSize) {
    // try {
    // if (this.xregistryClient == null) {
    // connectToXRegistry();
    // }
    // ResourceData[] resources = this.xregistryClient.findResource("");
    // LinkedList<Workflow> workflows = new LinkedList<Workflow>();
    // if (resultSetSize == -1) {
    // for (ResourceData resource : resources) {
    // QName resourceName = resource.getName();
    // if (resourceName != null
    // && XBayaConstants.LEAD_NS.equals(resourceName
    // .getNamespaceURI())) {
    // workflows.add(getWorkflow(resourceName));
    // }
    // }
    // } else {
    // for (int i = 0; i < Math.min(resources.length, resultSetSize); i++) {
    // ResourceData resource = resources[i];
    // QName resourceName = resource.getName();
    // if (resourceName != null
    // && XBayaConstants.LEAD_NS.equals(resourceName
    // .getNamespaceURI())) {
    // workflows.add(getWorkflow(resourceName));
    // }
    // }
    // }
    //
    // return workflows.toArray(new Workflow[0]);
    // } catch (XRegistryClientException e) {
    // throw new XBayaRuntimeException(e);
    // }
    // }

    /**
     * Returns the workflow object fetched from Xregistry.
     * 
     * @param xRegistryURI
     * @param gssCredential
     *            Proxy credential
     * @param name
     * @return
     */
    public Workflow getWorkflow(String name) {
        return getWorkflow(new QName(XBayaConstants.LEAD_NS, name));
    }

    public void main() {

        XBayaConfiguration config = null;
        config.setMyProxyServer("myproxy.teragrid.org");
        config.setMyProxyUsername("USER");
        config.setMyProxyPassphrase("PASSWORD");

        new XBayaEngine(config);
    }
}