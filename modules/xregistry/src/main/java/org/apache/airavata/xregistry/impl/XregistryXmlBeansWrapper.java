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
package org.apache.airavata.xregistry.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.XregistryConstants.DocType;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.doc.DocData;
import org.apache.airavata.xregistry.doc.ResourceUtils;
import org.apache.airavata.xregistry.utils.Utils;

import xregistry.generated.AddCapabilityDocument;
import xregistry.generated.AddCapabilityResponseDocument;
import xregistry.generated.AddCapabilityTokenDocument;
import xregistry.generated.AddCapabilityTokenResponseDocument;
import xregistry.generated.AddGrouptoGroupDocument;
import xregistry.generated.AddGrouptoGroupResponseDocument;
import xregistry.generated.AddOGCEResourceDocument;
import xregistry.generated.AddOGCEResourceResponseDocument;
import xregistry.generated.AddResourceDocument;
import xregistry.generated.AddResourceResponseDocument;
import xregistry.generated.AddUsertoGroupDocument;
import xregistry.generated.AddUsertoGroupResponseDocument;
import xregistry.generated.App2HostsDocument;
import xregistry.generated.App2HostsResponseDocument;
import xregistry.generated.CapabilityToken;
import xregistry.generated.CreateGroupDocument;
import xregistry.generated.CreateGroupResponseDocument;
import xregistry.generated.CreateUserDocument;
import xregistry.generated.CreateUserResponseDocument;
import xregistry.generated.DeleteGroupDocument;
import xregistry.generated.DeleteGroupResponseDocument;
import xregistry.generated.DeleteUserDocument;
import xregistry.generated.DeleteUserResponseDocument;
import xregistry.generated.FindAppDescDocument;
import xregistry.generated.FindAppDescResponseDocument;
import xregistry.generated.FindHostsDocument;
import xregistry.generated.FindHostsResponseDocument;
import xregistry.generated.FindOGCEResourceDocument;
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
import xregistry.generated.GetAppDescResponseDocument;
import xregistry.generated.GetCapabilityDocument;
import xregistry.generated.GetCapabilityResponseDocument;
import xregistry.generated.GetConcreateWsdlDocument;
import xregistry.generated.GetConcreateWsdlResponseDocument;
import xregistry.generated.GetHostDescDocument;
import xregistry.generated.GetHostDescResponseDocument;
import xregistry.generated.GetOGCEResourceDocument;
import xregistry.generated.GetOGCEResourceResponseDocument;
import xregistry.generated.GetResourceDocument;
import xregistry.generated.GetResourceResponseDocument;
import xregistry.generated.GetServiceDescDocument;
import xregistry.generated.GetServiceDescResponseDocument;
import xregistry.generated.HostDescData;
import xregistry.generated.IsAuthorizedToAcssesDocument;
import xregistry.generated.IsAuthorizedToAcssesResponseDocument;
import xregistry.generated.ListGroupsDocument;
import xregistry.generated.ListGroupsGivenAUserDocument;
import xregistry.generated.ListGroupsGivenAUserResponseDocument;
import xregistry.generated.ListGroupsResponseDocument;
import xregistry.generated.ListSubActorsGivenAGroupDocument;
import xregistry.generated.ListSubActorsGivenAGroupResponseDocument;
import xregistry.generated.ListUsersDocument;
import xregistry.generated.ListUsersResponseDocument;
import xregistry.generated.OGCEResourceData;
import xregistry.generated.RegisterAppDescDocument;
import xregistry.generated.RegisterAppDescResponseDocument;
import xregistry.generated.RegisterConcreteWsdlDocument;
import xregistry.generated.RegisterConcreteWsdlResponseDocument;
import xregistry.generated.RegisterHostDescDocument;
import xregistry.generated.RegisterHostDescResponseDocument;
import xregistry.generated.RegisterServiceDescDocument;
import xregistry.generated.RegisterServiceDescResponseDocument;
import xregistry.generated.RemoveAppDescDocument;
import xregistry.generated.RemoveAppDescResponseDocument;
import xregistry.generated.RemoveCapabilityDocument;
import xregistry.generated.RemoveCapabilityResponseDocument;
import xregistry.generated.RemoveConcreteWsdlDocument;
import xregistry.generated.RemoveConcreteWsdlResponseDocument;
import xregistry.generated.RemoveGroupFromGroupDocument;
import xregistry.generated.RemoveGroupFromGroupResponseDocument;
import xregistry.generated.RemoveHostDescDocument;
import xregistry.generated.RemoveHostDescResponseDocument;
import xregistry.generated.RemoveOGCEResourceDocument;
import xregistry.generated.RemoveOGCEResourceResponseDocument;
import xregistry.generated.RemoveResourceDocument;
import xregistry.generated.RemoveResourceResponseDocument;
import xregistry.generated.RemoveServiceDescDocument;
import xregistry.generated.RemoveServiceDescResponseDocument;
import xregistry.generated.RemoveUserFromGroupDocument;
import xregistry.generated.RemoveUserFromGroupResponseDocument;
import xregistry.generated.ResourceData;
import xregistry.generated.ServiceDescData;
import xregistry.generated.WsdlData;
import xregistry.generated.AddCapabilityDocument.AddCapability;
import xregistry.generated.AddCapabilityTokenDocument.AddCapabilityToken;
import xregistry.generated.AddOGCEResourceDocument.AddOGCEResource;
import xregistry.generated.AddResourceDocument.AddResource;
import xregistry.generated.App2HostsResponseDocument.App2HostsResponse;
import xregistry.generated.FindAppDescResponseDocument.FindAppDescResponse;
import xregistry.generated.FindAppDescResponseDocument.FindAppDescResponse.AppData;
import xregistry.generated.FindHostsResponseDocument.FindHostsResponse;
import xregistry.generated.FindOGCEResourceDocument.FindOGCEResource;
import xregistry.generated.FindOGCEResourceResponseDocument.FindOGCEResourceResponse;
import xregistry.generated.FindResourceDocument.FindResource;
import xregistry.generated.FindResourceResponseDocument.FindResourceResponse;
import xregistry.generated.FindServiceDescResponseDocument.FindServiceDescResponse;
import xregistry.generated.FindServiceInstanceResponseDocument.FindServiceInstanceResponse;
import xregistry.generated.GetCapabilityDocument.GetCapability;
import xregistry.generated.GetCapabilityResponseDocument.GetCapabilityResponse;
import xregistry.generated.GetOGCEResourceDocument.GetOGCEResource;
import xregistry.generated.GetResourceDocument.GetResource;
import xregistry.generated.IsAuthorizedToAcssesDocument.IsAuthorizedToAcsses;
import xregistry.generated.ListGroupsGivenAUserResponseDocument.ListGroupsGivenAUserResponse;
import xregistry.generated.ListGroupsResponseDocument.ListGroupsResponse;
import xregistry.generated.ListSubActorsGivenAGroupDocument.ListSubActorsGivenAGroup;
import xregistry.generated.ListSubActorsGivenAGroupResponseDocument.ListSubActorsGivenAGroupResponse.Actor;
import xregistry.generated.ListUsersResponseDocument.ListUsersResponse;
import xregistry.generated.RemoveOGCEResourceDocument.RemoveOGCEResource;
import xregistry.generated.RemoveResourceDocument.RemoveResource;
import xsul.MLogger;
import xsul.message_router.MessageContext;

public class XregistryXmlBeansWrapper implements XregistryPortType {
    protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);

    private XregistryImpl registryImpl;

    public XregistryXmlBeansWrapper(GlobalContext globalContext) {
        registryImpl = new XregistryImpl(globalContext);
    }

    private String findUserDN() {
        String userDN = null;
         MessageContext mc = xsul.xhandler_context.ServerContextAccessHandler.getContext();
        if (mc != null) {
            userDN = mc.getIncomingUserDn();
        }
        
        if(userDN == null){
            userDN = XregistryConstants.ANONYMOUS_USER;
        }
        userDN = Utils.canonicalizeDN(userDN);
        log.info("Invoker = " + userDN);
        return userDN;
    }

    public AddCapabilityResponseDocument addCapability(AddCapabilityDocument capabilityDocument)
            throws XregistryException {
        log.info("Request: " + capabilityDocument.xmlText());
        AddCapability addCapability = capabilityDocument.getAddCapability();
        registryImpl.addCapability(findUserDN(), addCapability.getResourceID(), addCapability
                .getActor(), addCapability.getIsActorAUser(), addCapability.getAction());
        AddCapabilityResponseDocument responseDocument = AddCapabilityResponseDocument.Factory
                .newInstance();
        responseDocument.addNewAddCapabilityResponse();
        log.info("Response: " + responseDocument.xmlText());
        return responseDocument;
    }

    public AddCapabilityTokenResponseDocument addCapabilityToken(AddCapabilityTokenDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        AddCapabilityToken captoken = input.getAddCapabilityToken();

        Timestamp notBefore = new Timestamp(captoken.xgetNotbefore().getDateValue().getTime());
        Timestamp notAfter = new Timestamp(captoken.xgetNotafter().getDateValue().getTime());

        registryImpl.addCapability(findUserDN(), captoken.getResourceID(), captoken.getActor(),
                captoken.getActorType(), captoken.getAction(), captoken.getAssertions(), notBefore,
                notAfter);
        AddCapabilityTokenResponseDocument responseDocument = AddCapabilityTokenResponseDocument.Factory
                .newInstance();
        responseDocument.addNewAddCapabilityTokenResponse();
        log.info("Response: " + responseDocument.xmlText());
        return responseDocument;

    }

    public AddUsertoGroupResponseDocument addUsertoGroup(AddUsertoGroupDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.addUsertoGroup(findUserDN(), input.getAddUsertoGroup().getGroup(), input
                .getAddUsertoGroup().getUserToAdd());

        AddUsertoGroupResponseDocument responseDocument = AddUsertoGroupResponseDocument.Factory
                .newInstance();
        responseDocument.addNewAddUsertoGroupResponse();
        log.info("Response: " + responseDocument.xmlText());
        return responseDocument;
    }

    public App2HostsResponseDocument app2Hosts(App2HostsDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String[] hostList = registryImpl.app2Hosts(findUserDN(), input.getApp2Hosts().getAppName());

        App2HostsResponseDocument app2HostResDoc = App2HostsResponseDocument.Factory.newInstance();
        App2HostsResponse app2HostRes = app2HostResDoc.addNewApp2HostsResponse();

        if (hostList != null) {
            for (String host : hostList) {
                app2HostRes.addNewHost().setStringValue(host);
            }
        }
        log.info("Response:" + app2HostResDoc.xmlText());
        return app2HostResDoc;
    }

    public CreateGroupResponseDocument createGroup(CreateGroupDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.createGroup(findUserDN(), input.getCreateGroup().getGroupName(), input
                .getCreateGroup().getDescription());
        CreateGroupResponseDocument responseDocument = CreateGroupResponseDocument.Factory
                .newInstance();
        responseDocument.addNewCreateGroupResponse();
        log.info("Response: " + responseDocument.xmlText());
        return responseDocument;
    }

    public CreateUserResponseDocument createUser(CreateUserDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.createUser(findUserDN(), input.getCreateUser().getUserName(), input
                .getCreateUser().getDescription());
        CreateUserResponseDocument responseDocument = CreateUserResponseDocument.Factory
                .newInstance();
        responseDocument.addNewCreateUserResponse();
        log.info("Response: " + responseDocument.xmlText());
        return responseDocument;
    }

    public DeleteGroupResponseDocument deleteGroup(DeleteGroupDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.deleteGroup(findUserDN(), input.getDeleteGroup().getGroupName());
        DeleteGroupResponseDocument responseDocument = DeleteGroupResponseDocument.Factory
                .newInstance();
        responseDocument.addNewDeleteGroupResponse();
        log.info("Response: " + responseDocument.xmlText());
        return responseDocument;
    }

    public DeleteUserResponseDocument deleteUser(DeleteUserDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.deleteUser(findUserDN(), input.getDeleteUser().getUserName());
        DeleteUserResponseDocument responseDocument = DeleteUserResponseDocument.Factory
                .newInstance();
        responseDocument.addNewDeleteUserResponse();
        log.info("Response: " + responseDocument.xmlText());
        return responseDocument;
    }

    public FindAppDescResponseDocument findAppDesc(FindAppDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        List<org.apache.airavata.xregistry.doc.AppData> appsData = registryImpl.findAppDesc(findUserDN(), input.getFindAppDesc()
                .getAppNameSearchStr());
        FindAppDescResponseDocument res = FindAppDescResponseDocument.Factory.newInstance();

        FindAppDescResponse appDescResponse = res.addNewFindAppDescResponse();
        
        
        if (appsData != null) {
            for (org.apache.airavata.xregistry.doc.AppData appData : appsData) {
                AppData xbeansAppData = appDescResponse.addNewAppData();
                copyAppData(appData, xbeansAppData);
            }
        } else {
            res.addNewFindAppDescResponse();
        }
        log.info("Response: " + res.xmlText());
        return res;
    }

    public FindHostsResponseDocument findHosts(FindHostsDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        List<DocData> hostList = registryImpl.findHosts(findUserDN(), input.getFindHosts()
                .getHostNameSearchStr());

        FindHostsResponseDocument app2HostResDoc = FindHostsResponseDocument.Factory.newInstance();
        FindHostsResponse hostsRes = app2HostResDoc.addNewFindHostsResponse();
        if (hostList != null) {
            for (DocData hostdata : hostList) {
                HostDescData result = hostsRes.addNewResults();
                copyResourceData(hostdata, result);
            }
        }
        log.info("Response  Sent  " + app2HostResDoc.xmlText());
        return app2HostResDoc;
    }

    public FindServiceDescResponseDocument findServiceDesc(FindServiceDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        List<DocData> serviceList = registryImpl.findServiceDesc(findUserDN(), input
                .getFindServiceDesc().getServiceQNameSearchStr());

        FindServiceDescResponseDocument findServiceDescResponseDocument = FindServiceDescResponseDocument.Factory
                .newInstance();
        FindServiceDescResponse findServiceDescResponse = findServiceDescResponseDocument
                .addNewFindServiceDescResponse();
        if (serviceList != null) {
            for (DocData service : serviceList) {
                ServiceDescData serviceDescData = findServiceDescResponse.addNewServiceName();
                copyResourceData(service, serviceDescData);
            }
        }
        log.info("Response  Sent  " + findServiceDescResponseDocument.xmlText());
        return findServiceDescResponseDocument;
    }

    public FindServiceInstanceResponseDocument findServiceInstance(FindServiceInstanceDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        List<DocData> serviceInstanceList = registryImpl.findServiceInstance(findUserDN(), input
                .getFindServiceInstance().getServiceQnameSearchStr());

        FindServiceInstanceResponseDocument findServiceInstanceResponseDocument = FindServiceInstanceResponseDocument.Factory
                .newInstance();
        FindServiceInstanceResponse findServiceDescResponse = findServiceInstanceResponseDocument
                .addNewFindServiceInstanceResponse();
        if (serviceInstanceList != null) {
            for (DocData service : serviceInstanceList) {
                WsdlData wsdlData = findServiceDescResponse.addNewServiceInstance();
                copyResourceData(service, wsdlData);
            }
        }
        log.info("Response  Sent  " + findServiceInstanceResponseDocument.xmlText());
        return findServiceInstanceResponseDocument;
    }

    public GetAbstractWsdlResponseDocument getAbstractWsdl(GetAbstractWsdlDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String wsdlAsStr = registryImpl.getAbstractWsdl(findUserDN(), input.getGetAbstractWsdl()
                .getWsdlQName().toString());
        GetAbstractWsdlResponseDocument responseDocument = GetAbstractWsdlResponseDocument.Factory
                .newInstance();
        if (wsdlAsStr != null) {
            responseDocument.addNewGetAbstractWsdlResponse().setWsdlAsStr(wsdlAsStr);
        } else {
            responseDocument.addNewGetAbstractWsdlResponse().setNilWsdlAsStr();
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public GetAppDescResponseDocument getAppDesc(GetAppDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String appDescAsStr = registryImpl.getAppDesc(findUserDN(), input.getGetAppDesc()
                .getAppQName(), input.getGetAppDesc().getHostName());
        GetAppDescResponseDocument responseDocument = GetAppDescResponseDocument.Factory
                .newInstance();
        if (appDescAsStr != null) {
            responseDocument.addNewGetAppDescResponse().setAppdescAsStr(appDescAsStr);
        } else {
            responseDocument.addNewGetAppDescResponse().setNilAppdescAsStr();
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public GetCapabilityResponseDocument getCapability(GetCapabilityDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        GetCapability capabilityData = input.getGetCapability();
        CapabilityToken[] capabilities = registryImpl.getCapability(findUserDN(), capabilityData
                .getResourceID(), capabilityData.getUser(), capabilityData.getActorType(),
                capabilityData.getAction());

        GetCapabilityResponseDocument responseDocument = GetCapabilityResponseDocument.Factory
                .newInstance();
        GetCapabilityResponse capabilityResponse = responseDocument.addNewGetCapabilityResponse();
        if (capabilities != null) {
            capabilityResponse.setTokenArray(capabilities);
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public GetConcreateWsdlResponseDocument getConcreateWsdl(GetConcreateWsdlDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String cwsdlAsStr = registryImpl.getConcreateWsdl(findUserDN(), input.getGetConcreateWsdl()
                .getWsdlQname().toString());
        GetConcreateWsdlResponseDocument responseDocument = GetConcreateWsdlResponseDocument.Factory
                .newInstance();
        if (cwsdlAsStr == null) {
            responseDocument.addNewGetConcreateWsdlResponse().setNilWsdlAsStr();
        } else {
            responseDocument.addNewGetConcreateWsdlResponse().setWsdlAsStr(cwsdlAsStr);
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public GetHostDescResponseDocument getHostDesc(GetHostDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String hostDescAsStr = registryImpl.getHostDesc(findUserDN(), input.getGetHostDesc()
                .getHostName());

        GetHostDescResponseDocument responseDocument = GetHostDescResponseDocument.Factory
                .newInstance();
        if (hostDescAsStr != null) {
            responseDocument.addNewGetHostDescResponse().setHostDescAsStr(hostDescAsStr);
        } else {
            responseDocument.addNewGetHostDescResponse().setNilHostDescAsStr();
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public GetServiceDescResponseDocument getServiceDesc(GetServiceDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String serviceDescAsStr = registryImpl.getServiceDesc(findUserDN(), input
                .getGetServiceDesc().getServiceQname().toString());
        GetServiceDescResponseDocument responseDocument = GetServiceDescResponseDocument.Factory
                .newInstance();
        if (serviceDescAsStr != null) {
            responseDocument.addNewGetServiceDescResponse().setServiceDescAsStr(serviceDescAsStr);
        } else {
            responseDocument.addNewGetServiceDescResponse().setNilServiceDescAsStr();
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public ListGroupsResponseDocument listGroups(ListGroupsDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String[] groups = registryImpl.listGroups(findUserDN());
        ListGroupsResponseDocument responseDocument = ListGroupsResponseDocument.Factory
                .newInstance();
        ListGroupsResponse listGroupsResponse = responseDocument.addNewListGroupsResponse();

        if (groups != null) {
            for (String group : groups) {
                listGroupsResponse.addNewGroup().setStringValue(group);
            }
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public ListGroupsGivenAUserResponseDocument listGroupsGivenAUser(
            ListGroupsGivenAUserDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String[] groups = registryImpl.listGroupsGivenAUser(findUserDN(), input
                .getListGroupsGivenAUser().getUserName());
        ListGroupsGivenAUserResponseDocument responseDocument = ListGroupsGivenAUserResponseDocument.Factory
                .newInstance();
        ListGroupsGivenAUserResponse listGroupsResponse = responseDocument
                .addNewListGroupsGivenAUserResponse();

        if (groups != null) {
            for (String group : groups) {
                listGroupsResponse.addNewGroup().setStringValue(group);
            }
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public ListUsersResponseDocument listUsers(ListUsersDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String[] users = registryImpl.listUsers(findUserDN());
        ListUsersResponseDocument responseDocument = ListUsersResponseDocument.Factory
                .newInstance();
        ListUsersResponse listGroupsResponse = responseDocument.addNewListUsersResponse();

        if (users != null) {
            for (String user : users) {
                listGroupsResponse.addNewUser().setStringValue(user);
            }
        }
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    
    
    public ListSubActorsGivenAGroupResponseDocument listSubActorsGivenAGroup(ListSubActorsGivenAGroupDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        ListSubActorsGivenAGroup listSubActorsGivenAGroup = input.getListSubActorsGivenAGroup();
        Actor[] actors = registryImpl.listSubActorsGivenAGroup(findUserDN(), listSubActorsGivenAGroup.getGroup());

        ListSubActorsGivenAGroupResponseDocument responseDocument = ListSubActorsGivenAGroupResponseDocument.Factory
                .newInstance();
        responseDocument.addNewListSubActorsGivenAGroupResponse().setActorArray(actors);
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }


    public RegisterAppDescResponseDocument registerAppDesc(RegisterAppDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String resourceID = registryImpl.registerAppDesc(findUserDN(), input.getRegisterAppDesc()
                .getAppDescAsStr());
        RegisterAppDescResponseDocument responseDocument = RegisterAppDescResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRegisterAppDescResponse().setResourceID(resourceID);
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RegisterConcreteWsdlResponseDocument registerConcreteWsdl(
            RegisterConcreteWsdlDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String userDN = findUserDN();
        String resourceID = registryImpl.registerConcreteWsdl(userDN, input
                .getRegisterConcreteWsdl().getWsdlAsStr(), input.getRegisterConcreteWsdl()
                .getLifetimeAsSeconds());
        registryImpl.addCapability(userDN, resourceID, XregistryConstants.PUBLIC_GROUP, 
                false, XregistryConstants.Action.Read.toString());
        RegisterConcreteWsdlResponseDocument responseDocument = RegisterConcreteWsdlResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRegisterConcreteWsdlResponse().setResourceID(resourceID);
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RegisterHostDescResponseDocument registerHostDesc(RegisterHostDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String resourceID = registryImpl.registerHostDesc(findUserDN(), input.getRegisterHostDesc()
                .getHostDescAsStr());
        RegisterHostDescResponseDocument responseDocument = RegisterHostDescResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRegisterHostDescResponse().setResourceID(resourceID);
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RegisterServiceDescResponseDocument registerServiceDesc(RegisterServiceDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String resourceID = registryImpl.registerServiceDesc(findUserDN(), input
                .getRegisterServiceDesc().getServiceDescAsStr(), input.getRegisterServiceDesc()
                .getAwsdlAdStr());
        RegisterServiceDescResponseDocument responseDocument = RegisterServiceDescResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRegisterServiceDescResponse().setResourceID(resourceID);
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveAppDescResponseDocument removeAppDesc(RemoveAppDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.removeAppDesc(findUserDN(), input.getRemoveAppDesc().getAppName().toString(), input
                .getRemoveAppDesc().getHostName());
        RemoveAppDescResponseDocument responseDocument = RemoveAppDescResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRemoveAppDescResponse();
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveCapabilityResponseDocument removeCapability(RemoveCapabilityDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.removeCapability(findUserDN(), input.getRemoveCapability().getResourceID(),
                input.getRemoveCapability().getActor());
        RemoveCapabilityResponseDocument responseDocument = RemoveCapabilityResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRemoveCapabilityResponse();
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveConcreteWsdlResponseDocument removeConcreteWsdl(RemoveConcreteWsdlDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        String userdn = findUserDN();
        String wsdlQname = input.getRemoveConcreteWsdl().getWsdlQName().toString();
        String resourceID = ResourceUtils.getResourceID(DocType.CWsdl,wsdlQname);
        registryImpl.removeCapability(userdn, resourceID, XregistryConstants.PUBLIC_GROUP);
        registryImpl.removeConcreteWsdl(userdn, wsdlQname);
        RemoveConcreteWsdlResponseDocument responseDocument = RemoveConcreteWsdlResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRemoveConcreteWsdlResponse();
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveGroupFromGroupResponseDocument removeGroupFromGroup(
            RemoveGroupFromGroupDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.removeGroupFromGroup(findUserDN(), input.getRemoveGroupFromGroup()
                .getMasterGroup(), input.getRemoveGroupFromGroup().getGroupToremove());
        RemoveGroupFromGroupResponseDocument responseDocument = RemoveGroupFromGroupResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRemoveGroupFromGroupResponse();
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveHostDescResponseDocument removeHostDesc(RemoveHostDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.removeHostDesc(findUserDN(), input.getRemoveHostDesc().getHostName());
        RemoveHostDescResponseDocument responseDocument = RemoveHostDescResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRemoveHostDescResponse();
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveServiceDescResponseDocument removeServiceDesc(RemoveServiceDescDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl
                .removeServiceDesc(findUserDN(), input.getRemoveServiceDesc().getServiceQname().toString());
        RemoveServiceDescResponseDocument responseDocument = RemoveServiceDescResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRemoveServiceDescResponse();
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveUserFromGroupResponseDocument removeUserFromGroup(RemoveUserFromGroupDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.removeUserFromGroup(findUserDN(), input.getRemoveUserFromGroup()
                .getGroupName(), input.getRemoveUserFromGroup().getUserName());
        RemoveUserFromGroupResponseDocument responseDocument = RemoveUserFromGroupResponseDocument.Factory
                .newInstance();
        responseDocument.addNewRemoveUserFromGroupResponse();
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;

    }

    public AddGrouptoGroupResponseDocument addGrouptoGroup(AddGrouptoGroupDocument input)
            throws XregistryException {
        log.info("Request Received " + input.xmlText());
        registryImpl.addAGroupToGroup(findUserDN(), input.getAddGrouptoGroup().getGroup(), input
                .getAddGrouptoGroup().getGroupToAdd());
        AddGrouptoGroupResponseDocument responseDocument = AddGrouptoGroupResponseDocument.Factory
                .newInstance();
        responseDocument.addNewAddGrouptoGroupResponse();
        log.info("Response  Sent  " + responseDocument.xmlText());
        return responseDocument;
    }

    public IsAuthorizedToAcssesResponseDocument isAuthorizedToAcsses(
            IsAuthorizedToAcssesDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        IsAuthorizedToAcsses isAuthorizedToAcsses = input.getIsAuthorizedToAcsses();
        boolean decision = registryImpl
                .isAuthorizedToAcsses(findUserDN(), isAuthorizedToAcsses.getResourceID(),
                        isAuthorizedToAcsses.getActor(), isAuthorizedToAcsses.getAction());
        IsAuthorizedToAcssesResponseDocument responseDocument = IsAuthorizedToAcssesResponseDocument.Factory
                .newInstance();
        responseDocument.addNewIsAuthorizedToAcssesResponse().setDecision(decision);
        log.info("Response " + responseDocument.xmlText());
        return responseDocument;
    }

    public AddResourceResponseDocument addResource(AddResourceDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        AddResource resource = input.getAddResource();
        registryImpl.registerDocument(findUserDN(), resource.getResourceName(), resource.getResourceAsStr());
        AddResourceResponseDocument responseDocument = AddResourceResponseDocument.Factory.newInstance();
        responseDocument.addNewAddResourceResponse();
        log.info("Response " + responseDocument.xmlText());
        return responseDocument;
    }

    public FindResourceResponseDocument findResource(FindResourceDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        FindResource resource = input.getFindResource();
        List<DocData> list = registryImpl.findDocument(findUserDN(), resource.getResourceNameQuery());
        
        FindResourceResponseDocument responseDocument = FindResourceResponseDocument.Factory.newInstance();
        FindResourceResponse findResourceResponse = responseDocument.addNewFindResourceResponse();
        if(list != null){
            for(DocData data:list){
                ResourceData resourceData = findResourceResponse.addNewResource();
                copyResourceData(data, resourceData);
            }
        }
        log.info("Response " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveResourceResponseDocument removeResource(RemoveResourceDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        RemoveResource resource = input.getRemoveResource();
        registryImpl.removeDocument(findUserDN(), resource.getResourceName());
        RemoveResourceResponseDocument responseDocument = RemoveResourceResponseDocument.Factory.newInstance();
        responseDocument.addNewRemoveResourceResponse();
        log.info("Response " + responseDocument.xmlText());
        return responseDocument;

    }

    public GetResourceResponseDocument getResource(GetResourceDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        GetResource resource = input.getGetResource();
        GetResourceResponseDocument responseDocument = GetResourceResponseDocument.Factory.newInstance();
        responseDocument.addNewGetResourceResponse().setResourceAsStr(registryImpl.getDocument(findUserDN(), resource.getResourceName()));
        log.info("Response " + responseDocument.xmlText());
        return responseDocument;
    }
    
    private void copyAppData(org.apache.airavata.xregistry.doc.AppData data,AppData xbeansAppData){
        copyResourceData(data, xbeansAppData);
        xbeansAppData.setHostName(data.secondryName);
    }
    
    private void copyResourceData(DocData data,ResourceData resourceData){
        resourceData.setAllowedAction(data.allowedAction);
        resourceData.setOwner(data.owner);
        resourceData.setResourceID(data.resourceID.toString());
        resourceData.setName(data.name);
    }

    public XregistryImpl getRegistryImpl() {
        return registryImpl;
    }
    public AddOGCEResourceResponseDocument addOGCEResource(AddOGCEResourceDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        AddOGCEResource resource = input.getAddOGCEResource();
        String userDN = null;
        if(resource.getOwner() != null && resource.getOwner() != ""){
        	userDN = resource.getOwner();
        }else{
        	userDN = findUserDN();
        }
        String parentTypedID = resource.getParentTypedID();
        if(parentTypedID == null || parentTypedID.trim() == "" )
        {
        	parentTypedID = XregistryConstants.DEFAULTPARENT;
        }
        registryImpl.registerOGCEResource(userDN, resource.getResourceID(),resource.getResourceName(),  resource.getResourceType(),resource.getResourceDesc(), resource.getResourceDocument(), parentTypedID);
        AddOGCEResourceResponseDocument responseDocument = AddOGCEResourceResponseDocument.Factory.newInstance();
        responseDocument.addNewAddOGCEResourceResponse();
        log.info("Response " + responseDocument.xmlText());
        return responseDocument;
    }
	public GetOGCEResourceResponseDocument getOGCEResource(GetOGCEResourceDocument input) throws XregistryException {
		log.info("Request Received " + input.xmlText());
	 	GetOGCEResource resource = input.getGetOGCEResource();
		GetOGCEResourceResponseDocument responseDocument = GetOGCEResourceResponseDocument.Factory
				.newInstance();
		 String parentTypedID = resource.getParentTypedID();
	        if(parentTypedID == null || parentTypedID.trim() == "" )
	        {
	        	parentTypedID = XregistryConstants.DEFAULTPARENT;
	        }
		responseDocument.addNewGetOGCEResourceResponse().setResourceAsStr(registryImpl.getOGCEResource(findUserDN(),
		 resource.getResourceID(), resource.getResourceType(), parentTypedID));
		log.info("Response " + responseDocument.xmlText());
		return responseDocument;

	}

    public FindOGCEResourceResponseDocument findOGCEResource(FindOGCEResourceDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        FindOGCEResource resource = input.getFindOGCEResource();
        String parentTypedID = resource.getParentTypedID();
        if(parentTypedID == null || parentTypedID.trim() == "" )
        {
        	parentTypedID = XregistryConstants.DEFAULTPARENT;
        }
        
        List<DocData> list = registryImpl.findOGCEResource(findUserDN(), resource.getResourceNameQuery(), resource.getResourceName() ,resource.getResourceType(),parentTypedID);
        
        FindOGCEResourceResponseDocument responseDocument = FindOGCEResourceResponseDocument.Factory.newInstance();
        FindOGCEResourceResponse findResourceResponse = responseDocument.addNewFindOGCEResourceResponse();
        if(list != null){
            for(DocData data:list){
            	OGCEResourceData resourceData = findResourceResponse.addNewResource();
                copyOGCEResourceData(data, resourceData);
            }
       }
        log.info("Response " + responseDocument.xmlText());
        return responseDocument;
    }

    public RemoveOGCEResourceResponseDocument removeOGCEResource(RemoveOGCEResourceDocument input) throws XregistryException {
        log.info("Request Received " + input.xmlText());
        RemoveOGCEResource resource = input.getRemoveOGCEResource();
        registryImpl.removeOGCEResource(findUserDN(), resource.getResourceID(), resource.getResourceType());
        RemoveOGCEResourceResponseDocument responseDocument = RemoveOGCEResourceResponseDocument.Factory.newInstance();
        responseDocument.addNewRemoveOGCEResourceResponse();
        log.info("Response " + responseDocument.xmlText());
        return responseDocument;

    }
    private void copyOGCEResourceData(DocData data,OGCEResourceData resourceData){
        resourceData.setAllowedAction(data.allowedAction);
        resourceData.setOwner(data.owner);
        resourceData.setResourceID(data.resourceID);
        resourceData.setResourceName(data.resourcename);
        resourceData.setName(data.resourceID);
        resourceData.setResourceType(data.resourcetype);
        resourceData.setResourceDesc(data.resourcedesc);
        resourceData.setCreated(data.created);
        
    }
}
