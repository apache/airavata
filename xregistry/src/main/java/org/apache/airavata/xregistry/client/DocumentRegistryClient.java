/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.airavata.xregistry.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.doc.DocData;
import org.apache.airavata.xregistry.impl.XregistryPortType;
import org.apache.airavata.xregistry.utils.Utils;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;

import xregistry.generated.AddCapabilityDocument;
import xregistry.generated.AddCapabilityDocument.AddCapability;
import xregistry.generated.AddOGCEResourceDocument;
import xregistry.generated.AddOGCEResourceDocument.AddOGCEResource;
import xregistry.generated.AddResourceDocument;
import xregistry.generated.AddResourceDocument.AddResource;
import xregistry.generated.App2HostsDocument;
import xregistry.generated.App2HostsResponseDocument;
import xregistry.generated.CapabilityToken;
import xregistry.generated.FindAppDescDocument;
import xregistry.generated.FindAppDescResponseDocument;
import xregistry.generated.FindAppDescResponseDocument.FindAppDescResponse.AppData;
import xregistry.generated.FindHostsDocument;
import xregistry.generated.FindHostsResponseDocument;
import xregistry.generated.FindOGCEResourceDocument;
import xregistry.generated.FindOGCEResourceDocument.FindOGCEResource;
import xregistry.generated.FindOGCEResourceResponseDocument;
import xregistry.generated.FindResourceDocument;
import xregistry.generated.FindResourceResponseDocument;
import xregistry.generated.FindServiceDescDocument;
import xregistry.generated.FindServiceDescResponseDocument;
import xregistry.generated.FindServiceInstanceDocument;
import xregistry.generated.FindServiceInstanceResponseDocument;
import xregistry.generated.GetAbstractWsdlDocument;
import xregistry.generated.GetAbstractWsdlResponseDocument;
import xregistry.generated.GetAppDescDocument;
import xregistry.generated.GetAppDescDocument.GetAppDesc;
import xregistry.generated.GetCapabilityDocument;
import xregistry.generated.GetCapabilityDocument.GetCapability;
import xregistry.generated.GetCapabilityResponseDocument;
import xregistry.generated.GetConcreateWsdlDocument;
import xregistry.generated.GetHostDescDocument;
import xregistry.generated.GetOGCEResourceDocument;
import xregistry.generated.GetOGCEResourceDocument.GetOGCEResource;
import xregistry.generated.GetResourceDocument;
import xregistry.generated.GetServiceDescDocument;
import xregistry.generated.HostDescData;
import xregistry.generated.IsAuthorizedToAcssesDocument;
import xregistry.generated.IsAuthorizedToAcssesDocument.IsAuthorizedToAcsses;
import xregistry.generated.IsAuthorizedToAcssesResponseDocument;
import xregistry.generated.OGCEResourceData;
import xregistry.generated.RegisterAppDescDocument;
import xregistry.generated.RegisterConcreteWsdlDocument;
import xregistry.generated.RegisterConcreteWsdlDocument.RegisterConcreteWsdl;
import xregistry.generated.RegisterHostDescDocument;
import xregistry.generated.RegisterServiceDescDocument;
import xregistry.generated.RegisterServiceDescDocument.RegisterServiceDesc;
import xregistry.generated.RemoveAppDescDocument;
import xregistry.generated.RemoveAppDescDocument.RemoveAppDesc;
import xregistry.generated.RemoveCapabilityDocument;
import xregistry.generated.RemoveCapabilityDocument.RemoveCapability;
import xregistry.generated.RemoveConcreteWsdlDocument;
import xregistry.generated.RemoveHostDescDocument;
import xregistry.generated.RemoveOGCEResourceDocument;
import xregistry.generated.RemoveOGCEResourceDocument.RemoveOGCEResource;
import xregistry.generated.RemoveResourceDocument;
import xregistry.generated.RemoveServiceDescDocument;
import xregistry.generated.ResourceData;
import xregistry.generated.ServiceDescData;
import xregistry.generated.WsdlData;
import xsul.MLogger;
import xsul.xwsif_runtime.WSIFClient;

public class DocumentRegistryClient {
    protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);
    
    private XregistryPortType proxy;
    
    /**  
     * If this property is on, registry list only document own by the client. For most of the
     * use cases user do not need to see shared documents and therefore we keep the property set by defualt.
     */
    private String userDN; 
    
    public DocumentRegistryClient(String registryServiceWsdlUrl) throws XregistryException{
    	GlobalContext globalContext = new GlobalContext(true);
     	String trustedCA = System.getProperty("ssl.hostcertsKeyFile");
    	String hostCert =  System.getProperty("ssl.trustedCertsFile");
		globalContext.setTrustedCertsFile(trustedCA);
		globalContext.setHostcertsKeyFile(hostCert);
	    WSIFClient client = Utils.createWSIFClient(globalContext, registryServiceWsdlUrl);
        proxy = (XregistryPortType)client.generateDynamicStub(XregistryPortType.class);
        userDN = globalContext.getUserDN();
        System.out.println("Create Stub for "+ registryServiceWsdlUrl + " using "+ userDN);
    }
    public DocumentRegistryClient(GlobalContext context,String registryServiceWsdlUrl) throws XregistryException{
        WSIFClient client = Utils.createWSIFClient(context, registryServiceWsdlUrl);
        proxy = (XregistryPortType)client.generateDynamicStub(XregistryPortType.class);
        userDN = context.getUserDN();
        System.out.println("Create Stub for "+ registryServiceWsdlUrl + " using "+ userDN);
    }
    public String[]  app2Hosts(String appName) throws XregistryException {
        App2HostsDocument document = App2HostsDocument.Factory.newInstance();
        document.addNewApp2Hosts().setAppName(appName);
        App2HostsResponseDocument responseDocument = proxy.app2Hosts(document);
        return responseDocument.getApp2HostsResponse().getHostArray();
    }
    public void addCapability(String resource, String actor, boolean isUser, String action) throws XregistryException {
        AddCapabilityDocument document = AddCapabilityDocument.Factory.newInstance();
        AddCapability capability = document.addNewAddCapability();
        capability.setResourceID(resource);
        capability.setActor(actor);
        capability.setIsActorAUser(isUser);
        capability.setAction(action);
        proxy.addCapability(document);
    }
    
    /**
     * This is the search method for capabilities, the null values can be defined for most values to allow a search. But one of the 
     * resourceID or actor must present
     * @param user
     * @param resourceID
     * @param actor and actorType - Both or non must precent .. can be Null
     * @param action 
     * @return
     * @throws XregistryException
     */
    
    public CapabilityToken[] findCapability(String resourceID, String actor, boolean actorType, String action) throws XregistryException {
        GetCapabilityDocument document = GetCapabilityDocument.Factory.newInstance();
        GetCapability getCapability = document.addNewGetCapability();
        if(resourceID != null || actor != null){
            getCapability.setUser(actor);
            getCapability.setResourceID(resourceID);
            getCapability.setActorType(actorType);
            getCapability.setAction(action);
            GetCapabilityResponseDocument capabilityResponse = proxy.getCapability(document);
            return capabilityResponse.getGetCapabilityResponse().getTokenArray();
        }else{
            throw new XregistryException("one of the resourceID or actor must present");
        }
    }
    
    
    
    public void removeCapability(String resourceID,String actor) throws XregistryException{
        RemoveCapabilityDocument document = RemoveCapabilityDocument.Factory.newInstance();
        RemoveCapability removeCapability = document.addNewRemoveCapability();
        removeCapability.setResourceID(resourceID);
        removeCapability.setActor(actor);
        proxy.removeCapability(document);
    }
    
    
    
    
    public org.apache.airavata.xregistry.doc.AppData[] findAppDesc(String query) throws XregistryException {
        FindAppDescDocument document = FindAppDescDocument.Factory.newInstance();
        document.addNewFindAppDesc().setAppNameSearchStr(query);
        FindAppDescResponseDocument responseDocument = proxy.findAppDesc(document);
        
        AppData[] results = responseDocument.getFindAppDescResponse().getAppDataArray();
        
        
        if(results != null){
            List<org.apache.airavata.xregistry.doc.AppData> finalResults = new ArrayList<org.apache.airavata.xregistry.doc.AppData>();
            for(int i = 0;i<results.length;i++){
                try {
					AppData xbeansData = results[i];
					org.apache.airavata.xregistry.doc.AppData resultAppData = new org.apache.airavata.xregistry.doc.AppData(xbeansData.getName(),xbeansData.getOwner(),xbeansData.getHostName());
					resultAppData.allowedAction = xbeansData.getAllowedAction();
					resultAppData.resourceID = xbeansData.getName();
					finalResults.add(resultAppData);
				} catch (XmlValueOutOfRangeException e) {
					log.severe("Problem with retrieving object : " + e.getLocalizedMessage(), e);
				}    
            }
            return finalResults.toArray(new org.apache.airavata.xregistry.doc.AppData[0]);
        }else{
            return null;
        }
    }
    public DocData[]  findHosts(String hostName) throws XregistryException {
        FindHostsDocument document = FindHostsDocument.Factory.newInstance();
        document.addNewFindHosts().setHostNameSearchStr(hostName);
        FindHostsResponseDocument responseDocument = proxy.findHosts(document);
        
        HostDescData[] hostDescData =  responseDocument.getFindHostsResponse().getResultsArray();
        if(hostDescData == null){
            return null;
        }

        List<DocData> results = new ArrayList<DocData>();
        for(int i = 0;i<hostDescData.length;i++){
        	try {
            HostDescData host = hostDescData[i];
            DocData data = new DocData(new QName(host.getResourceID()),host.getOwner());
            data.allowedAction = host.getAllowedAction();
            data.resourceID = new QName(host.getResourceID());
            results.add(data);
        	} catch (XmlValueOutOfRangeException e) {
				log.severe("Problem with retrieving object : " + e.getLocalizedMessage(), e);
			}   
        }
        return results.toArray(new DocData[0]);
    }
    
    public DocData[]  findResource(String resourceNameQuery) throws XregistryException {
        FindResourceDocument document = FindResourceDocument.Factory.newInstance();
        document.addNewFindResource().setResourceNameQuery(resourceNameQuery);
        FindResourceResponseDocument responseDocument = proxy.findResource(document);
        
        ResourceData[] resourceData =  responseDocument.getFindResourceResponse().getResourceArray();
        if(resourceData == null){
            return null;
        }
        List<DocData> results = new ArrayList<DocData>();
        for(int i = 0;i<resourceData.length;i++){
            try {
        	ResourceData host = resourceData[i];
            DocData data = new DocData(host.getName(),host.getOwner());
            data.allowedAction = host.getAllowedAction();
            data.resourceID = host.getName();
            data.resourcename = host.getResourceID();
            results.add(data);
            } catch (XmlValueOutOfRangeException e) {
 				log.severe("Problem with retrieving object : " + e.getLocalizedMessage(), e);
 			}   
        }
        return results.toArray(new DocData[0]);
    }
    
    public DocData[]  findServiceDesc(String serviceName) throws XregistryException {
        FindServiceDescDocument document = FindServiceDescDocument.Factory.newInstance();
        document.addNewFindServiceDesc().setServiceQNameSearchStr(serviceName);
        FindServiceDescResponseDocument responseDocument = proxy.findServiceDesc(document);
        
        ServiceDescData[] serviceDescData =  responseDocument.getFindServiceDescResponse().getServiceNameArray();
        
        if(serviceDescData == null){
            return null;
        }
        List<DocData> results = new ArrayList<DocData>();
        for(int i = 0;i<serviceDescData.length;i++){
        	try {
            DocData data = new DocData(serviceDescData[i].getName(),serviceDescData[i].getOwner());
            data.allowedAction = serviceDescData[i].getAllowedAction();
            data.resourceID = serviceDescData[i].getName();
            results.add(data);
        	} catch (XmlValueOutOfRangeException e) {
   				log.severe("Problem with retrieving object : " + e.getLocalizedMessage(), e);
   			}   
        }
        return results.toArray(new DocData[0]);
    }
    public DocData[]  findServiceInstance(String serviceName) throws XregistryException {
        FindServiceInstanceDocument document = FindServiceInstanceDocument.Factory.newInstance();
        document.addNewFindServiceInstance().setServiceQnameSearchStr(serviceName);
        FindServiceInstanceResponseDocument responseDocument = proxy.findServiceInstance(document);
       
        WsdlData[] wsdlDescData =  responseDocument.getFindServiceInstanceResponse().getServiceInstanceArray();
        if(wsdlDescData == null){
            return null;
        }
        List<DocData> results = new ArrayList<DocData>();
        for(int i = 0;i<wsdlDescData.length;i++){
        	try {
            DocData data = new DocData(wsdlDescData[i].getName(),wsdlDescData[i].getOwner());
            data.allowedAction = wsdlDescData[i].getAllowedAction();
            data.resourceID = wsdlDescData[i].getName();
            results.add(data); 
        	} catch (XmlValueOutOfRangeException e) {
   				log.severe("Problem with retrieving object : " + e.getLocalizedMessage(), e);
   			}               
        }
        return results.toArray(new DocData[0]);
    }
    public String getAbstractWsdl(QName wsdlQName) throws XregistryException {
        GetAbstractWsdlDocument document = GetAbstractWsdlDocument.Factory.newInstance();
        document.addNewGetAbstractWsdl().setWsdlQName(wsdlQName);
        GetAbstractWsdlResponseDocument responseDocument = proxy.getAbstractWsdl(document);
        return responseDocument.getGetAbstractWsdlResponse().getWsdlAsStr();
    }
    public String getAppDesc(String appName, String hostName) throws XregistryException {
        GetAppDescDocument document = GetAppDescDocument.Factory.newInstance();
        GetAppDesc getAppDesc = document.addNewGetAppDesc();
        getAppDesc.setAppQName(appName);
        getAppDesc.setHostName(hostName);
        return proxy.getAppDesc(document).getGetAppDescResponse().getAppdescAsStr();
    }
    public String getConcreateWsdl(QName wsdlQName) throws XregistryException {
        GetConcreateWsdlDocument document = GetConcreateWsdlDocument.Factory.newInstance();
        document.addNewGetConcreateWsdl().setWsdlQname(wsdlQName);
        return proxy.getConcreateWsdl(document).getGetConcreateWsdlResponse().getWsdlAsStr();
    }
    public String getHostDesc(String hostName) throws XregistryException {
        GetHostDescDocument document = GetHostDescDocument.Factory.newInstance();
        document.addNewGetHostDesc().setHostName(hostName);
        return proxy.getHostDesc(document).getGetHostDescResponse().getHostDescAsStr();
    }
    
    public String getResource(QName resourceName) throws XregistryException {
        GetResourceDocument document = GetResourceDocument.Factory.newInstance();
        document.addNewGetResource().setResourceName(resourceName);
        return proxy.getResource(document).getGetResourceResponse().getResourceAsStr();
    }
    
    public String getServiceDesc(QName serviceName) throws XregistryException {
        GetServiceDescDocument input =  GetServiceDescDocument.Factory.newInstance();
        input.addNewGetServiceDesc().setServiceQname(serviceName);
        return proxy.getServiceDesc(input).getGetServiceDescResponse().getServiceDescAsStr();
    }
    public String registerAppDesc(String appDescAsStr) throws XregistryException {
        RegisterAppDescDocument input =  RegisterAppDescDocument.Factory.newInstance();
        input.addNewRegisterAppDesc().setAppDescAsStr(appDescAsStr);
        return proxy.registerAppDesc(input).getRegisterAppDescResponse().getResourceID();
    }
    public String registerConcreteWsdl( String wsdlAsStr, int lifetimeAsSeconds) throws XregistryException {
        RegisterConcreteWsdlDocument input =  RegisterConcreteWsdlDocument.Factory.newInstance();
        RegisterConcreteWsdl concreteWsdl = input.addNewRegisterConcreteWsdl();
        concreteWsdl.setWsdlAsStr(wsdlAsStr);
        concreteWsdl.setLifetimeAsSeconds(lifetimeAsSeconds);
        return proxy.registerConcreteWsdl(input).getRegisterConcreteWsdlResponse().getResourceID();
    }
    public String registerHostDesc( String hostDescAsStr) throws XregistryException {
        RegisterHostDescDocument input =  RegisterHostDescDocument.Factory.newInstance();
        input.addNewRegisterHostDesc().setHostDescAsStr(hostDescAsStr);
        return proxy.registerHostDesc(input).getRegisterHostDescResponse().getResourceID();
    }
    
    public void registerResource(QName resourceName, String resourceAsStr) throws XregistryException {
        AddResourceDocument input = AddResourceDocument.Factory.newInstance();
        AddResource resource = input.addNewAddResource();
        resource.setResourceAsStr(resourceAsStr);
        resource.setResourceName(resourceName);
        proxy.addResource(input);
    }
    
    public String registerServiceDesc( String serviceDescAsStr, String awsdlAsStr) throws XregistryException {
        RegisterServiceDescDocument input =  RegisterServiceDescDocument.Factory.newInstance();
        RegisterServiceDesc serviceDesc = input.addNewRegisterServiceDesc();
        serviceDesc.setServiceDescAsStr(serviceDescAsStr);
        serviceDesc.setAwsdlAdStr(awsdlAsStr);
        return proxy.registerServiceDesc(input).getRegisterServiceDescResponse().getResourceID();
    }
    public void removeAppDesc(QName appName, String hostName) throws XregistryException {
        RemoveAppDescDocument input =  RemoveAppDescDocument.Factory.newInstance();
        RemoveAppDesc removeAppDesc = input.addNewRemoveAppDesc();
        removeAppDesc.setAppName(appName);
        removeAppDesc.setHostName(hostName);
        proxy.removeAppDesc(input);
        
    }
    public void removeConcreteWsdl(QName wsdlQName) throws XregistryException {
        RemoveConcreteWsdlDocument input =  RemoveConcreteWsdlDocument.Factory.newInstance();
        input.addNewRemoveConcreteWsdl().setWsdlQName(wsdlQName);
        proxy.removeConcreteWsdl(input);
    }
    public void removeHostDesc(String hostName) throws XregistryException {
        RemoveHostDescDocument input =  RemoveHostDescDocument.Factory.newInstance();
        input.addNewRemoveHostDesc().setHostName(hostName);
        proxy.removeHostDesc(input);        
    }
    public void removeServiceDesc(QName serviceName) throws XregistryException {
        RemoveServiceDescDocument input =  RemoveServiceDescDocument.Factory.newInstance();
        input.addNewRemoveServiceDesc().setServiceQname(serviceName);
        proxy.removeServiceDesc(input);
    }
    
    
    public void removeResource(QName resourceName) throws XregistryException {
        RemoveResourceDocument input =  RemoveResourceDocument.Factory.newInstance();
        input.addNewRemoveResource().setResourceName(resourceName);
        proxy.removeResource(input);
    }
    
    /**
     * Give a actor, and a resource, this decides does the actor have acess to the resource. The actor must be a user and not a group
     * @param user
     * @param resourceID
     * @param actor
     * @param action
     * @return
     * @throws XregistryException
     */
    
    public boolean isAuthorizedToAcsses(String user, String resourceID, String actor, String action) throws XregistryException {
        IsAuthorizedToAcssesDocument input = IsAuthorizedToAcssesDocument.Factory.newInstance();
        IsAuthorizedToAcsses isAuthorizedToAcsses = input.addNewIsAuthorizedToAcsses();
        isAuthorizedToAcsses.setActor(actor);
        isAuthorizedToAcsses.setResourceID(resourceID);
        isAuthorizedToAcsses.setAction(action);
        IsAuthorizedToAcssesResponseDocument responseDocument =  proxy.isAuthorizedToAcsses(input);
        return responseDocument.getIsAuthorizedToAcssesResponse().getDecision();
    }
    
    /**
     * 
     * @param resourceName
     * @param resourceType
     * @param resourceDesc
     * @throws XregistryException
     */
	public void registerOGCEResource(QName resourceID, String resourceName, String resourceType,
			String resourceDesc, String resourceDocument,
			String resourceParentTypedID, String owner)
			throws XregistryException {
		AddOGCEResourceDocument input = AddOGCEResourceDocument.Factory
				.newInstance();
		AddOGCEResource resource = input.addNewAddOGCEResource();
		resource.setResourceType(resourceType);
		resource.setResourceDesc(resourceDesc);
		resource.setResourceDocument(resourceDocument);
		resource.setResourceID(resourceID);
		resource.setResourceName(resourceName);
		if (owner != null && owner != "") {
			resource.setOwner(owner);
		}
		resource.setParentTypedID(resourceParentTypedID);
		proxy.addOGCEResource(input);
	}
    /**
     * 
     * @param resourceName
     * @return
     * @throws XregistryException
     */
    public String getOGCEResource(QName resourceID, String resourceType, String resourceParentTypedID) throws XregistryException {
        GetOGCEResourceDocument input = GetOGCEResourceDocument.Factory.newInstance();
        GetOGCEResource document = input.addNewGetOGCEResource();
        document.setResourceID(resourceID);
        document.setResourceType(resourceType);
        document.setParentTypedID(resourceParentTypedID);
        return proxy.getOGCEResource(input).getGetOGCEResourceResponse().getResourceAsStr();
    }
    
    /**
     * 
     * @param resourceNameQuery
     * @param resourceType
     * @return
     * @throws XregistryException
     */
    public DocData[]  findOGCEResource(String resourceNameQuery, String resourceType, String resourceParentTypedID) throws XregistryException {
        FindOGCEResourceDocument document = FindOGCEResourceDocument.Factory.newInstance();
        FindOGCEResource  findOGCEResource =  document.addNewFindOGCEResource();
        findOGCEResource.setResourceNameQuery(resourceNameQuery);
        findOGCEResource.setResourceType(resourceType);
        findOGCEResource.setResourceName(resourceNameQuery);
        findOGCEResource.setParentTypedID(resourceParentTypedID);
        
        FindOGCEResourceResponseDocument responseDocument = proxy.findOGCEResource(document);
        
        OGCEResourceData[] resourceData =  responseDocument.getFindOGCEResourceResponse().getResourceArray();
        if(resourceData == null){
            return null;
        }
        List<DocData> results = new ArrayList<DocData>();
        for(int i = 0;i<resourceData.length;i++){
        	try {
            OGCEResourceData resource = resourceData[i];
            DocData data = new DocData(resource.getName(),resource.getOwner());
            data.allowedAction = resource.getAllowedAction();
            data.resourceID = resource.getResourceID();
            data.resourcename = resource.getResourceName();
            data.created = resource.getCreated();
            data.resourcedesc = resource.getResourceDesc();
            data.resourcetype = resource.getResourceType();
            results.add(data);
        	} catch (XmlValueOutOfRangeException e) {
   				log.severe("Problem with retrieving object : " + e.getLocalizedMessage(), e);
   			}               
        }
        return results.toArray(new DocData[0]);
    }
    /**
     * 
     * @param resourceName
     * @throws XregistryException
     */
    public void removeOGCEResource(QName resourceID, String resourceType) throws XregistryException {
        RemoveOGCEResourceDocument input =  RemoveOGCEResourceDocument.Factory.newInstance();
        RemoveOGCEResource removeOGCEResource = input.addNewRemoveOGCEResource();
        removeOGCEResource.setResourceID(resourceID);
        removeOGCEResource.setResourceType(resourceType);
        proxy.removeOGCEResource(input);
    }
  
    
    
}

